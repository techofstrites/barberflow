package com.barberflow.chatbot.application

import com.barberflow.chatbot.application.intent.Intent
import com.barberflow.chatbot.application.intent.IntentResolver
import com.barberflow.chatbot.domain.model.Conversation
import com.barberflow.chatbot.domain.model.ConversationState
import com.barberflow.chatbot.domain.port.AppointmentBookingPort
import com.barberflow.chatbot.domain.port.AvailableSlotQueryPort
import com.barberflow.chatbot.domain.port.BookingRequest
import com.barberflow.chatbot.domain.port.ProfessionalQueryPort
import com.barberflow.chatbot.domain.port.WhatsAppButtonMessage
import com.barberflow.chatbot.domain.port.WhatsAppGateway
import com.barberflow.chatbot.domain.port.WhatsAppListMessage
import com.barberflow.chatbot.domain.port.WhatsAppTextMessage
import com.barberflow.chatbot.domain.port.ButtonOption
import com.barberflow.chatbot.domain.port.ListRow
import com.barberflow.chatbot.domain.port.ListSection
import com.barberflow.chatbot.domain.repository.ConversationRepository
import com.barberflow.core.tenant.TenantId
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@Service
class ConversationOrchestrator(
    private val conversationRepository: ConversationRepository,
    private val intentResolver: IntentResolver,
    private val whatsAppGateway: WhatsAppGateway,
    private val professionalQueryPort: ProfessionalQueryPort,
    private val availableSlotQueryPort: AvailableSlotQueryPort,
    private val appointmentBookingPort: AppointmentBookingPort,
    private val eventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val ptBr = Locale.forLanguageTag("pt-BR")
    private val slotFormatter = DateTimeFormatter.ofPattern("EEE, dd/MM 'às' HH:mm", ptBr)
    private val summaryFormatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'às' HH:mm", ptBr)

    @Transactional
    fun handleIncomingMessage(tenantId: TenantId, customerPhone: String, messageBody: String) {
        val conversation = conversationRepository.findActiveByPhone(customerPhone, tenantId)
            ?: Conversation.start(tenantId, customerPhone)

        if (conversation.isExpired()) conversation.reset()

        conversation.addInboundMessage(messageBody)

        val intent = intentResolver.resolve(messageBody, conversation.state)
        log.debug("State={} Intent={} phone={}", conversation.state, intent::class.simpleName, customerPhone)

        when (intent) {
            is Intent.Greeting -> handleGreeting(conversation)
            is Intent.ScheduleAppointment -> handleScheduleRequest(tenantId, conversation)
            is Intent.SelectProfessional -> handleProfessionalSelected(tenantId, conversation, intent.professionalId)
            is Intent.SelectSlot -> handleSlotSelected(conversation, intent.slotId)
            is Intent.Confirm -> handleConfirmation(tenantId, conversation)
            is Intent.Decline -> sendMainMenu(conversation, "Tudo bem! Se precisar de algo, é só chamar. 👊")
            is Intent.CancelAppointment -> handleCancelRequest(conversation)
            is Intent.Unrecognized -> handleUnrecognized(tenantId, conversation)
            else -> handleUnrecognized(tenantId, conversation)
        }

        conversationRepository.save(conversation)
        conversation.domainEvents.forEach { eventPublisher.publishEvent(it) }
        conversation.clearEvents()
    }

    // ── Greeting ────────────────────────────────────────────────────────────
    private fun handleGreeting(conversation: Conversation) {
        conversation.transitionTo(ConversationState.GREETING)
        conversation.resetUnrecognized()
        sendMainMenuButtons(conversation, "Olá! Bem-vindo. O que você gostaria de fazer?")
    }

    // ── Schedule: list professionals ─────────────────────────────────────────
    private fun handleScheduleRequest(tenantId: TenantId, conversation: Conversation) {
        val professionals = professionalQueryPort.findActiveProfessionals(tenantId)

        if (professionals.isEmpty()) {
            sendMainMenu(conversation, "Não há profissionais disponíveis no momento. Tente mais tarde.")
            return
        }

        conversation.transitionTo(ConversationState.PROFESSIONAL_SELECTION)
        conversation.resetUnrecognized()

        val rows = professionals.map { p ->
            ListRow(
                id = "professional:${p.id}",
                title = p.name,
                description = if (p.specialties.isNotEmpty()) p.specialties.joinToString(", ") else null
            )
        }

        whatsAppGateway.sendList(
            WhatsAppListMessage(
                to = conversation.customerPhone,
                body = "Com qual profissional você quer agendar?",
                buttonText = "Ver profissionais",
                sections = listOf(ListSection(title = "Equipe", rows = rows))
            )
        )
    }

    // ── Professional selected: show available slots ───────────────────────────
    private fun handleProfessionalSelected(tenantId: TenantId, conversation: Conversation, professionalId: String) {
        val pid = runCatching { UUID.fromString(professionalId) }.getOrNull()
            ?: return handleUnrecognized(tenantId, conversation)

        val professional = professionalQueryPort.findById(tenantId, pid)
            ?: return handleUnrecognized(tenantId, conversation)

        val slots = availableSlotQueryPort.findNextSlots(tenantId, pid, daysAhead = 7, limit = 3)

        if (slots.isEmpty()) {
            whatsAppGateway.sendButtons(
                WhatsAppButtonMessage(
                    to = conversation.customerPhone,
                    body = "Não encontrei horários disponíveis para ${professional.name} nos próximos 7 dias. Deseja escolher outro profissional?",
                    buttons = listOf(
                        ButtonOption("schedule", "Escolher outro"),
                        ButtonOption("decline:", "Voltar ao menu")
                    )
                )
            )
            return
        }

        conversation.updateContext { it.copy(selectedProfessionalId = pid, selectedProfessionalName = professional.name) }
        conversation.transitionTo(ConversationState.SLOT_SELECTION)
        conversation.resetUnrecognized()

        val zone = ZoneId.systemDefault()
        val buttons = slots.map { slot ->
            val epochSeconds = slot.startAt.toEpochSecond()
            val label = slot.startAt.withZoneSameInstant(zone).format(slotFormatter)
                .replaceFirstChar { it.uppercaseChar() }
            ButtonOption("slot:$epochSeconds", label)
        }

        whatsAppGateway.sendButtons(
            WhatsAppButtonMessage(
                to = conversation.customerPhone,
                body = "Ótimo! Aqui estão os horários disponíveis com ${professional.name}:",
                buttons = buttons
            )
        )
    }

    // ── Slot selected: show confirmation summary ─────────────────────────────
    private fun handleSlotSelected(conversation: Conversation, slotId: String) {
        val epochSeconds = slotId.toLongOrNull()
            ?: return sendMainMenu(conversation, "Horário inválido. Vamos recomeçar.")

        val startAt = java.time.Instant.ofEpochSecond(epochSeconds)
            .atZone(ZoneId.systemDefault())
        val endAt = startAt.plusMinutes(30)
        val professionalName = conversation.context.selectedProfessionalName ?: "profissional"

        conversation.updateContext { it.copy(selectedSlot = startAt, selectedSlotEnd = endAt) }
        conversation.transitionTo(ConversationState.AWAITING_CONFIRMATION)
        conversation.resetUnrecognized()

        val dateLabel = startAt.format(summaryFormatter).replaceFirstChar { it.uppercaseChar() }

        whatsAppGateway.sendButtons(
            WhatsAppButtonMessage(
                to = conversation.customerPhone,
                body = "Confirme o agendamento:\n\n👤 $professionalName\n📅 $dateLabel\n⏱ 30 minutos\n\nDeseja confirmar?",
                buttons = listOf(
                    ButtonOption("confirm:yes", "Confirmar ✅"),
                    ButtonOption("decline:", "Cancelar ❌")
                )
            )
        )
    }

    // ── Confirmation: book appointment ───────────────────────────────────────
    private fun handleConfirmation(tenantId: TenantId, conversation: Conversation) {
        val slot = conversation.context.selectedSlot
        val professionalId = conversation.context.selectedProfessionalId

        if (slot == null || professionalId == null) {
            sendMainMenu(conversation, "Ops! Perdi os dados do agendamento. Vamos recomeçar.")
            return
        }

        try {
            val appointmentId = appointmentBookingPort.book(
                BookingRequest(
                    tenantId = tenantId,
                    customerPhone = conversation.customerPhone,
                    professionalId = professionalId,
                    startAt = slot
                )
            )
            conversation.updateContext { it.copy(pendingAppointmentId = appointmentId) }
            conversation.transitionTo(ConversationState.COMPLETED)
            conversation.resetUnrecognized()

            val professionalName = conversation.context.selectedProfessionalName ?: "profissional"
            val zone = ZoneId.systemDefault()
            val dateLabel = slot.withZoneSameInstant(zone).format(summaryFormatter)
                .replaceFirstChar { it.uppercaseChar() }

            whatsAppGateway.sendText(
                WhatsAppTextMessage(
                    to = conversation.customerPhone,
                    body = "✅ Agendamento confirmado!\n\n👤 $professionalName\n📅 $dateLabel\n\nTe esperamos! Qualquer dúvida, é só chamar. 👊"
                )
            )
        } catch (e: IllegalArgumentException) {
            log.warn("Booking conflict for phone={}: {}", conversation.customerPhone, e.message)
            conversation.transitionTo(ConversationState.SLOT_SELECTION)
            val pid = professionalId
            whatsAppGateway.sendText(
                WhatsAppTextMessage(
                    to = conversation.customerPhone,
                    body = "Opa! Esse horário acabou de ser reservado. Vou buscar outros horários disponíveis..."
                )
            )
            // Re-query slots
            val slots = availableSlotQueryPort.findNextSlots(tenantId, pid, daysAhead = 7, limit = 3)
            if (slots.isEmpty()) {
                sendMainMenu(conversation, "Não há mais horários disponíveis nos próximos dias. 😔")
                return
            }
            val zone = ZoneId.systemDefault()
            val buttons = slots.map { s ->
                ButtonOption("slot:${s.startAt.toEpochSecond()}",
                    s.startAt.withZoneSameInstant(zone).format(slotFormatter).replaceFirstChar { it.uppercaseChar() })
            }
            whatsAppGateway.sendButtons(
                WhatsAppButtonMessage(
                    to = conversation.customerPhone,
                    body = "Escolha um dos horários disponíveis:",
                    buttons = buttons
                )
            )
        }
    }

    // ── Cancel existing appointment ──────────────────────────────────────────
    private fun handleCancelRequest(conversation: Conversation) {
        sendMainMenu(conversation, "Para cancelar um agendamento, entre em contato diretamente com a barbearia. Em breve teremos essa opção aqui! 😊")
    }

    // ── Unrecognized: always go back to main menu ────────────────────────────
    private fun handleUnrecognized(tenantId: TenantId, conversation: Conversation) {
        conversation.incrementUnrecognized()
        log.debug("Unrecognized message #{} for phone={}", conversation.unrecognizedCount, conversation.customerPhone)
        sendMainMenu(conversation, "Não entendi. O que gostaria de fazer?")
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private fun sendMainMenu(conversation: Conversation, text: String) {
        conversation.transitionTo(ConversationState.GREETING)
        sendMainMenuButtons(conversation, text)
    }

    private fun sendMainMenuButtons(conversation: Conversation, text: String) {
        whatsAppGateway.sendButtons(
            WhatsAppButtonMessage(
                to = conversation.customerPhone,
                body = text,
                buttons = listOf(
                    ButtonOption("schedule", "Agendar horário"),
                    ButtonOption("cancel", "Cancelar agendamento"),
                    ButtonOption("human", "Falar com atendente")
                )
            )
        )
    }
}
