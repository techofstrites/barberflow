package com.barberflow.chatbot.application

import com.barberflow.chatbot.application.intent.Intent
import com.barberflow.chatbot.application.intent.IntentResolver
import com.barberflow.chatbot.domain.model.Conversation
import com.barberflow.chatbot.domain.model.ConversationState
import com.barberflow.chatbot.domain.port.AppointmentBookingPort
import com.barberflow.chatbot.domain.port.AppointmentCancelPort
import com.barberflow.chatbot.domain.port.AppointmentQueryPort
import com.barberflow.chatbot.domain.port.AvailableSlotQueryPort
import com.barberflow.chatbot.domain.port.BookingRequest
import com.barberflow.chatbot.domain.port.CustomerResolverPort
import com.barberflow.chatbot.domain.port.ProfessionalQueryPort
import com.barberflow.chatbot.domain.port.ServiceQueryPort
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
import java.time.LocalDate
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
    private val serviceQueryPort: ServiceQueryPort,
    private val availableSlotQueryPort: AvailableSlotQueryPort,
    private val appointmentBookingPort: AppointmentBookingPort,
    private val appointmentQueryPort: AppointmentQueryPort,
    private val appointmentCancelPort: AppointmentCancelPort,
    private val customerResolverPort: CustomerResolverPort,
    private val eventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val ptBr = Locale.forLanguageTag("pt-BR")
    private val dayFormatter = DateTimeFormatter.ofPattern("EEE, dd/MM", ptBr)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", ptBr)
    private val slotFormatter = DateTimeFormatter.ofPattern("EEE, dd/MM 'às' HH:mm", ptBr)
    private val summaryFormatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'às' HH:mm", ptBr)
    private val historyFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", ptBr)

    @Transactional
    fun handleIncomingMessage(tenantId: TenantId, customerPhone: String, messageBody: String, contactName: String? = null) {
        // Auto-register customer on first contact, using WhatsApp profile name if available
        runCatching { customerResolverPort.findOrCreate(tenantId, customerPhone, contactName) }
            .onFailure { log.warn("Could not register customer phone={}: {}", customerPhone, it.message) }

        val conversation = conversationRepository.findActiveByPhone(customerPhone, tenantId)
            ?: Conversation.start(tenantId, customerPhone)

        if (conversation.isExpired()) conversation.reset()

        conversation.addInboundMessage(messageBody)

        val intent = intentResolver.resolve(messageBody, conversation.state)
        log.debug("State={} Intent={} phone={}", conversation.state, intent::class.simpleName, customerPhone)

        when (intent) {
            is Intent.Greeting -> handleGreeting(conversation)
            is Intent.ScheduleAppointment -> handleScheduleRequest(tenantId, conversation)
            is Intent.SelectService -> handleServiceSelected(tenantId, conversation, intent.serviceId)
            is Intent.SelectProfessional -> handleProfessionalSelected(tenantId, conversation, intent.professionalId)
            is Intent.SelectDay -> handleDaySelected(tenantId, conversation, intent.dateStr)
            is Intent.SelectSlot -> handleSlotSelected(conversation, intent.slotId)
            is Intent.Confirm -> when (conversation.state) {
                ConversationState.AWAITING_CONFIRMATION -> handleConfirmation(tenantId, conversation)
                ConversationState.AWAITING_CANCEL_CONFIRMATION -> handleCancelConfirmed(tenantId, conversation)
                else -> handleUnrecognized(tenantId, conversation)
            }
            is Intent.Decline -> when (conversation.state) {
                ConversationState.AWAITING_CANCEL_CONFIRMATION ->
                    sendMainMenu(conversation, "Tudo bem! O agendamento foi mantido. 👍")
                else -> sendMainMenu(conversation, "Tudo bem! Se precisar de algo, é só chamar. 👊")
            }
            is Intent.CancelAppointment -> handleCancelRequest(tenantId, conversation)
            is Intent.SelectCancelAppointment -> handleCancelSelected(tenantId, conversation, intent.appointmentId)
            is Intent.ViewHistory -> handleViewHistory(tenantId, conversation)
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
        sendMainMenuList(conversation, "Olá! Bem-vindo. O que você gostaria de fazer?")
    }

    // ── Schedule: list services ───────────────────────────────────────────────
    private fun handleScheduleRequest(tenantId: TenantId, conversation: Conversation) {
        val services = serviceQueryPort.findAll(tenantId)

        if (services.isEmpty()) {
            sendMainMenu(conversation, "Não há serviços disponíveis no momento. Tente mais tarde.")
            return
        }

        conversation.transitionTo(ConversationState.SERVICE_SELECTION)
        conversation.resetUnrecognized()

        val rows = services.map { s ->
            val priceLabel = "R$ ${s.price.setScale(0, java.math.RoundingMode.HALF_UP)}"
            ListRow(
                id = "service:${s.id}",
                title = s.name.cap(),
                description = "$priceLabel · ${s.durationMinutes} min"
            )
        }

        whatsAppGateway.sendList(
            WhatsAppListMessage(
                to = conversation.customerPhone,
                body = "Qual serviço você deseja?",
                buttonText = "Ver serviços",
                sections = listOf(ListSection(title = "Serviços disponíveis", rows = rows))
            )
        )
    }

    // ── Service selected: list professionals ──────────────────────────────────
    private fun handleServiceSelected(tenantId: TenantId, conversation: Conversation, serviceId: String) {
        val sid = runCatching { UUID.fromString(serviceId) }.getOrNull()
            ?: return handleUnrecognized(tenantId, conversation)

        val service = serviceQueryPort.findAll(tenantId).find { it.id == sid }
            ?: return handleUnrecognized(tenantId, conversation)

        conversation.updateContext {
            it.copy(
                selectedServiceId = sid,
                selectedServiceName = service.name,
                selectedServicePrice = service.price,
                selectedServiceDurationMinutes = service.durationMinutes
            )
        }

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
                title = p.name.cap(),
                description = if (p.specialties.isNotEmpty()) p.specialties.joinToString(", ").cap(72) else null
            )
        }

        whatsAppGateway.sendList(
            WhatsAppListMessage(
                to = conversation.customerPhone,
                body = "Com qual profissional você quer agendar *${service.name}*?",
                buttonText = "Ver profissionais",
                sections = listOf(ListSection(title = "Equipe", rows = rows))
            )
        )
    }

    // ── Professional selected: show available days ────────────────────────────
    private fun handleProfessionalSelected(tenantId: TenantId, conversation: Conversation, professionalId: String) {
        val pid = runCatching { UUID.fromString(professionalId) }.getOrNull()
            ?: return handleUnrecognized(tenantId, conversation)

        val professional = professionalQueryPort.findById(tenantId, pid)
            ?: return handleUnrecognized(tenantId, conversation)

        val duration = conversation.context.selectedServiceDurationMinutes
        val days = availableSlotQueryPort.findAvailableDays(tenantId, pid, daysAhead = 14, durationMinutes = duration)

        if (days.isEmpty()) {
            whatsAppGateway.sendButtons(
                WhatsAppButtonMessage(
                    to = conversation.customerPhone,
                    body = "Não encontrei dias disponíveis para ${professional.name} nos próximos 14 dias. Deseja escolher outro profissional?",
                    buttons = listOf(
                        ButtonOption("schedule", "Escolher outro"),
                        ButtonOption("decline:", "Voltar ao menu")
                    )
                )
            )
            return
        }

        conversation.updateContext { it.copy(selectedProfessionalId = pid, selectedProfessionalName = professional.name) }
        conversation.transitionTo(ConversationState.DAY_SELECTION)
        conversation.resetUnrecognized()

        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        val rows = days.take(10).map { date ->
            val label = when (date) {
                today -> "Hoje, ${date.format(dayFormatter)}"
                today.plusDays(1) -> "Amanhã, ${date.format(dayFormatter)}"
                else -> date.atStartOfDay(zone).format(dayFormatter).replaceFirstChar { it.uppercaseChar() }
            }
            ListRow(id = "day:$date", title = label)
        }

        whatsAppGateway.sendList(
            WhatsAppListMessage(
                to = conversation.customerPhone,
                body = "Ótimo! ${professional.name} tem disponibilidade nos seguintes dias:",
                buttonText = "Ver dias disponíveis",
                sections = listOf(ListSection(title = "Dias disponíveis", rows = rows))
            )
        )
    }

    // ── Day selected: show time slots for that day ────────────────────────────
    private fun handleDaySelected(tenantId: TenantId, conversation: Conversation, dateStr: String) {
        val date = runCatching { LocalDate.parse(dateStr) }.getOrNull()
            ?: return sendMainMenu(conversation, "Data inválida. Vamos recomeçar.")

        val pid = conversation.context.selectedProfessionalId
            ?: return sendMainMenu(conversation, "Ops! Perdi os dados. Vamos recomeçar.")
        val professionalName = conversation.context.selectedProfessionalName ?: "profissional"

        val duration = conversation.context.selectedServiceDurationMinutes
        val slots = availableSlotQueryPort.findSlotsForDay(tenantId, pid, date, durationMinutes = duration)

        if (slots.isEmpty()) {
            val days = availableSlotQueryPort.findAvailableDays(tenantId, pid, daysAhead = 14, durationMinutes = duration)
            if (days.isEmpty()) {
                sendMainMenu(conversation, "Não há mais horários disponíveis. 😔")
                return
            }
            val today = LocalDate.now()
            val zone = ZoneId.systemDefault()
            val rows = days.take(10).map { d ->
                val label = when (d) {
                    today -> "Hoje, ${d.format(dayFormatter)}"
                    today.plusDays(1) -> "Amanhã, ${d.format(dayFormatter)}"
                    else -> d.atStartOfDay(zone).format(dayFormatter).replaceFirstChar { it.uppercaseChar() }
                }
                ListRow(id = "day:$d", title = label)
            }
            whatsAppGateway.sendList(
                WhatsAppListMessage(
                    to = conversation.customerPhone,
                    body = "Esse dia não tem mais horários. Escolha outro dia:",
                    buttonText = "Ver dias disponíveis",
                    sections = listOf(ListSection(title = "Dias disponíveis", rows = rows))
                )
            )
            return
        }

        conversation.updateContext { it.copy(selectedDate = date) }
        conversation.transitionTo(ConversationState.SLOT_SELECTION)
        conversation.resetUnrecognized()

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val dayLabel = when (date) {
            today -> "hoje"
            today.plusDays(1) -> "amanhã"
            else -> "em ${date.format(dayFormatter)}"
        }

        val rows = slots.take(10).map { slot ->
            ListRow(
                id = "slot:${slot.startAt.toEpochSecond()}",
                title = slot.startAt.withZoneSameInstant(zone).format(timeFormatter)
            )
        }

        whatsAppGateway.sendList(
            WhatsAppListMessage(
                to = conversation.customerPhone,
                body = "Horários disponíveis com $professionalName $dayLabel:",
                buttonText = "Ver horários",
                sections = listOf(ListSection(title = date.format(dayFormatter).replaceFirstChar { it.uppercaseChar() }, rows = rows))
            )
        )
    }

    // ── Slot selected: show confirmation summary ─────────────────────────────
    private fun handleSlotSelected(conversation: Conversation, slotId: String) {
        val epochSeconds = slotId.toLongOrNull()
            ?: return sendMainMenu(conversation, "Horário inválido. Vamos recomeçar.")

        val startAt = java.time.Instant.ofEpochSecond(epochSeconds)
            .atZone(ZoneId.systemDefault())
        val duration = conversation.context.selectedServiceDurationMinutes
        val endAt = startAt.plusMinutes(duration.toLong())
        val professionalName = conversation.context.selectedProfessionalName ?: "profissional"
        val serviceName = conversation.context.selectedServiceName ?: "Serviço"
        val servicePrice = conversation.context.selectedServicePrice

        conversation.updateContext { it.copy(selectedSlot = startAt, selectedSlotEnd = endAt) }
        conversation.transitionTo(ConversationState.AWAITING_CONFIRMATION)
        conversation.resetUnrecognized()

        val dateLabel = startAt.format(summaryFormatter).replaceFirstChar { it.uppercaseChar() }
        val priceLabel = if (servicePrice != null)
            "\n💈 $serviceName — R$ ${servicePrice.setScale(2, java.math.RoundingMode.HALF_UP)}"
        else
            "\n💈 $serviceName"

        whatsAppGateway.sendButtons(
            WhatsAppButtonMessage(
                to = conversation.customerPhone,
                body = "Confirme o agendamento:\n\n👤 $professionalName\n📅 $dateLabel\n⏱ $duration min$priceLabel\n\nDeseja confirmar?",
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
                    startAt = slot,
                    durationMinutes = conversation.context.selectedServiceDurationMinutes,
                    serviceId = conversation.context.selectedServiceId,
                    serviceName = conversation.context.selectedServiceName,
                    servicePrice = conversation.context.selectedServicePrice
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
        } catch (e: Exception) {
            val isConflict = e is IllegalArgumentException && e.message?.contains("available", ignoreCase = true) == true
            if (isConflict) {
                log.warn("Booking conflict for phone={}: {}", conversation.customerPhone, e.message)
            } else {
                log.error("Booking failed for phone={}: {}", conversation.customerPhone, e.message, e)
            }
            conversation.transitionTo(ConversationState.SLOT_SELECTION)
            val pid = professionalId
            val errorBody = if (isConflict)
                "Opa! Esse horário acabou de ser reservado. Vou buscar outros horários disponíveis..."
            else
                "Ocorreu um erro ao confirmar o agendamento. Vamos tentar outro horário."
            whatsAppGateway.sendText(
                WhatsAppTextMessage(to = conversation.customerPhone, body = errorBody)
            )
            val days = availableSlotQueryPort.findAvailableDays(tenantId, pid, daysAhead = 14)
            if (days.isEmpty()) {
                sendMainMenu(conversation, "Não há mais horários disponíveis nos próximos dias. 😔")
                return
            }
            val today = LocalDate.now()
            val zone = ZoneId.systemDefault()
            conversation.transitionTo(ConversationState.DAY_SELECTION)
            val rows = days.take(10).map { d ->
                val label = when (d) {
                    today -> "Hoje, ${d.format(dayFormatter)}"
                    today.plusDays(1) -> "Amanhã, ${d.format(dayFormatter)}"
                    else -> d.atStartOfDay(zone).format(dayFormatter).replaceFirstChar { it.uppercaseChar() }
                }
                ListRow(id = "day:$d", title = label)
            }
            whatsAppGateway.sendList(
                WhatsAppListMessage(
                    to = conversation.customerPhone,
                    body = "Escolha outro dia disponível:",
                    buttonText = "Ver dias disponíveis",
                    sections = listOf(ListSection(title = "Dias disponíveis", rows = rows))
                )
            )
        }
    }

    // ── Cancel: list upcoming appointments ───────────────────────────────────
    private fun handleCancelRequest(tenantId: TenantId, conversation: Conversation) {
        val upcoming = appointmentQueryPort.findUpcoming(tenantId, conversation.customerPhone)

        if (upcoming.isEmpty()) {
            sendMainMenu(conversation, "Você não tem agendamentos próximos para cancelar. 😊")
            return
        }

        conversation.transitionTo(ConversationState.CANCEL_SELECTION)
        conversation.resetUnrecognized()

        val zone = ZoneId.systemDefault()
        val rows = upcoming.map { appt ->
            val dateLabel = appt.startAt.withZoneSameInstant(zone).format(historyFormatter)
            val fullTitle = "${appt.professionalName} - $dateLabel"
            ListRow(
                id = "cancel_appt:${appt.id}",
                title = if (fullTitle.length <= 24) fullTitle else fullTitle.take(21) + "..."
            )
        }

        whatsAppGateway.sendList(
            WhatsAppListMessage(
                to = conversation.customerPhone,
                body = "Selecione o agendamento que deseja cancelar:",
                buttonText = "Ver agendamentos",
                sections = listOf(ListSection(title = "Agendamentos próximos", rows = rows))
            )
        )
    }

    // ── Cancel selected: confirm ──────────────────────────────────────────────
    private fun handleCancelSelected(tenantId: TenantId, conversation: Conversation, appointmentId: String) {
        val apptId = runCatching { UUID.fromString(appointmentId) }.getOrNull()
            ?: return sendMainMenu(conversation, "Agendamento inválido. Vamos recomeçar.")

        val upcoming = appointmentQueryPort.findUpcoming(tenantId, conversation.customerPhone)
        val appt = upcoming.find { it.id == apptId }
            ?: return sendMainMenu(conversation, "Agendamento não encontrado. Vamos recomeçar.")

        val zone = ZoneId.systemDefault()
        val dateLabel = appt.startAt.withZoneSameInstant(zone).format(summaryFormatter).replaceFirstChar { it.uppercaseChar() }

        conversation.updateContext { it.copy(pendingCancelAppointmentId = apptId) }
        conversation.transitionTo(ConversationState.AWAITING_CANCEL_CONFIRMATION)
        conversation.resetUnrecognized()

        whatsAppGateway.sendButtons(
            WhatsAppButtonMessage(
                to = conversation.customerPhone,
                body = "Confirme o cancelamento:\n\n👤 ${appt.professionalName}\n📅 $dateLabel\n\nDeseja cancelar este agendamento?",
                buttons = listOf(
                    ButtonOption("confirm:cancel", "Sim, cancelar ❌"),
                    ButtonOption("decline:", "Não, manter ✅")
                )
            )
        )
    }

    // ── Cancel confirmed ──────────────────────────────────────────────────────
    private fun handleCancelConfirmed(tenantId: TenantId, conversation: Conversation) {
        val apptId = conversation.context.pendingCancelAppointmentId
            ?: return sendMainMenu(conversation, "Ops! Perdi os dados. Vamos recomeçar.")

        try {
            appointmentCancelPort.cancel(tenantId, apptId)
            conversation.updateContext { it.copy(pendingCancelAppointmentId = null) }
            sendMainMenu(conversation, "✅ Agendamento cancelado com sucesso! Se quiser reagendar, é só chamar. 😊")
        } catch (e: Exception) {
            log.error("Cancel failed for apptId={}: {}", apptId, e.message, e)
            sendMainMenu(conversation, "Ocorreu um erro ao cancelar. Tente novamente ou entre em contato com a barbearia.")
        }
    }

    // ── View history ──────────────────────────────────────────────────────────
    private fun handleViewHistory(tenantId: TenantId, conversation: Conversation) {
        val recent = appointmentQueryPort.findRecent(tenantId, conversation.customerPhone)

        if (recent.isEmpty()) {
            sendMainMenu(conversation, "Você ainda não possui agendamentos. 😊")
            return
        }

        val zone = ZoneId.systemDefault()
        val statusLabels = mapOf(
            "PENDING_CONFIRMATION" to "⏳ Aguardando",
            "CONFIRMED" to "✅ Confirmado",
            "IN_PROGRESS" to "✂️ Em andamento",
            "COMPLETED" to "✅ Concluído",
            "CANCELLED" to "❌ Cancelado",
            "NO_SHOW" to "⚠️ Não compareceu"
        )

        val lines = recent.mapIndexed { i, appt ->
            val dateLabel = appt.startAt.withZoneSameInstant(zone).format(historyFormatter)
            val statusLabel = statusLabels[appt.status] ?: appt.status
            "${i + 1}. 📅 $dateLabel — ${appt.professionalName}\n   $statusLabel"
        }.joinToString("\n\n")

        sendMainMenu(conversation, "📋 *Seus últimos agendamentos:*\n\n$lines")
    }

    // ── Unrecognized ─────────────────────────────────────────────────────────
    private fun handleUnrecognized(tenantId: TenantId, conversation: Conversation) {
        conversation.incrementUnrecognized()
        log.debug("Unrecognized message #{} for phone={}", conversation.unrecognizedCount, conversation.customerPhone)
        sendMainMenu(conversation, "Não entendi. O que gostaria de fazer?")
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private fun sendMainMenu(conversation: Conversation, text: String) {
        conversation.transitionTo(ConversationState.GREETING)
        sendMainMenuList(conversation, text)
    }

    /** Truncates a string to [max] chars, adding "…" if needed. WhatsApp list rows allow max 24 chars. */
    private fun String.cap(max: Int = 24) = if (length <= max) this else take(max - 1) + "…"

    private fun sendMainMenuList(conversation: Conversation, text: String) {
        whatsAppGateway.sendList(
            WhatsAppListMessage(
                to = conversation.customerPhone,
                body = text,
                buttonText = "Menu",
                sections = listOf(
                    ListSection(
                        title = "O que deseja fazer?",
                        rows = listOf(
                            ListRow("schedule", "Agendar horário"),
                            ListRow("cancel", "Cancelar agendamento"),
                            ListRow("history", "Histórico"),
                            ListRow("human", "Falar com atendente")
                        )
                    )
                )
            )
        )
    }
}
