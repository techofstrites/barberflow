package com.barberflow.chatbot.application

import com.barberflow.chatbot.application.intent.Intent
import com.barberflow.chatbot.application.intent.IntentResolver
import com.barberflow.chatbot.domain.model.Conversation
import com.barberflow.chatbot.domain.model.ConversationState
import com.barberflow.chatbot.domain.port.WhatsAppGateway
import com.barberflow.chatbot.domain.port.WhatsAppTextMessage
import com.barberflow.chatbot.domain.port.WhatsAppButtonMessage
import com.barberflow.chatbot.domain.port.ButtonOption
import com.barberflow.chatbot.domain.repository.ConversationRepository
import com.barberflow.core.tenant.TenantId
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ConversationOrchestrator(
    private val conversationRepository: ConversationRepository,
    private val intentResolver: IntentResolver,
    private val whatsAppGateway: WhatsAppGateway,
    private val eventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun handleIncomingMessage(tenantId: TenantId, customerPhone: String, messageBody: String) {
        val conversation = conversationRepository.findActiveByPhone(customerPhone, tenantId)
            ?: Conversation.start(tenantId, customerPhone)

        if (conversation.isExpired()) {
            conversation.reset()
        }

        conversation.addInboundMessage(messageBody)

        val intent = intentResolver.resolve(messageBody, conversation.state)
        log.debug("Resolved intent: {} for state: {}", intent, conversation.state)

        when (intent) {
            is Intent.Unrecognized -> handleUnrecognized(conversation)
            is Intent.Greeting -> handleGreeting(conversation)
            is Intent.ScheduleAppointment -> handleScheduleRequest(conversation)
            is Intent.Confirm -> handleConfirmation(conversation)
            is Intent.Decline -> handleDecline(conversation)
            is Intent.AnotherTime -> handleAnotherTime(conversation)
            is Intent.SelectService -> handleServiceSelection(conversation, intent.serviceId)
            is Intent.SelectProfessional -> handleProfessionalSelection(conversation, intent.professionalId)
            is Intent.SelectSlot -> handleSlotSelection(conversation, intent.slotId)
            is Intent.CancelAppointment -> handleCancelRequest(conversation)
            else -> handleUnrecognized(conversation)
        }

        conversationRepository.save(conversation)
        conversation.domainEvents.forEach { eventPublisher.publishEvent(it) }
        conversation.clearEvents()
    }

    private fun handleGreeting(conversation: Conversation) {
        conversation.transitionTo(ConversationState.GREETING)
        whatsAppGateway.sendButtons(
            WhatsAppButtonMessage(
                to = conversation.customerPhone,
                body = "Olá! Bem-vindo. O que você gostaria de fazer?",
                buttons = listOf(
                    ButtonOption("schedule", "Agendar horário"),
                    ButtonOption("cancel", "Cancelar agendamento"),
                    ButtonOption("info", "Informações")
                )
            )
        )
    }

    private fun handleScheduleRequest(conversation: Conversation) {
        conversation.transitionTo(ConversationState.SERVICE_SELECTION)
        whatsAppGateway.sendText(
            WhatsAppTextMessage(
                to = conversation.customerPhone,
                body = "Ótimo! Qual serviço você deseja?\n\nResponda com o número:\n1️⃣ Corte Social - R\$45\n2️⃣ Barba Terapia - R\$35\n3️⃣ Combo (Corte + Barba) - R\$70"
            )
        )
    }

    private fun handleServiceSelection(conversation: Conversation, serviceId: String) {
        conversation.updateContext { it.copy(selectedServiceIds = listOf(java.util.UUID.fromString(serviceId))) }
        conversation.transitionTo(ConversationState.SLOT_SELECTION)
        whatsAppGateway.sendButtons(
            WhatsAppButtonMessage(
                to = conversation.customerPhone,
                body = "Baseado na sua preferência, que tal um destes horários?",
                buttons = listOf(
                    ButtonOption("slot:thursday_18", "Quinta, 18:00"),
                    ButtonOption("slot:friday_17", "Sexta, 17:30"),
                    ButtonOption("slot:other", "Ver outros horários")
                )
            )
        )
    }

    private fun handleProfessionalSelection(conversation: Conversation, professionalId: String) {
        conversation.updateContext { it.copy(selectedProfessionalId = java.util.UUID.fromString(professionalId)) }
        conversation.transitionTo(ConversationState.SLOT_SELECTION)
        whatsAppGateway.sendText(
            WhatsAppTextMessage(
                to = conversation.customerPhone,
                body = "Perfeito! Quando você gostaria de agendar? Responda com a data e horário desejados."
            )
        )
    }

    private fun handleSlotSelection(conversation: Conversation, slotId: String) {
        conversation.transitionTo(ConversationState.AWAITING_CONFIRMATION)
        whatsAppGateway.sendButtons(
            WhatsAppButtonMessage(
                to = conversation.customerPhone,
                body = "Ótimo! Vou confirmar o agendamento:\n\n📅 Horário selecionado\n\nDeseja confirmar?",
                buttons = listOf(
                    ButtonOption("confirm:yes", "Confirmar ✅"),
                    ButtonOption("confirm:no", "Cancelar ❌")
                )
            )
        )
    }

    private fun handleConfirmation(conversation: Conversation) {
        conversation.transitionTo(ConversationState.COMPLETED)
        whatsAppGateway.sendText(
            WhatsAppTextMessage(
                to = conversation.customerPhone,
                body = "✅ Agendamento confirmado! Até lá. Qualquer dúvida, é só chamar!"
            )
        )
    }

    private fun handleDecline(conversation: Conversation) {
        conversation.transitionTo(ConversationState.IDLE)
        whatsAppGateway.sendText(
            WhatsAppTextMessage(
                to = conversation.customerPhone,
                body = "Tudo bem! Se precisar de algo, é só chamar. 👊"
            )
        )
    }

    private fun handleAnotherTime(conversation: Conversation) {
        conversation.transitionTo(ConversationState.SLOT_SELECTION)
        whatsAppGateway.sendText(
            WhatsAppTextMessage(
                to = conversation.customerPhone,
                body = "Claro! Me diga qual data e horário prefere e verifico a disponibilidade."
            )
        )
    }

    private fun handleCancelRequest(conversation: Conversation) {
        whatsAppGateway.sendText(
            WhatsAppTextMessage(
                to = conversation.customerPhone,
                body = "Para cancelar, me informe o dia do seu agendamento."
            )
        )
    }

    private fun handleUnrecognized(conversation: Conversation) {
        conversation.incrementUnrecognized()
        if (conversation.shouldTransferToHuman()) {
            conversation.transitionTo(ConversationState.TRANSFERRED_TO_HUMAN)
            whatsAppGateway.sendText(
                WhatsAppTextMessage(
                    to = conversation.customerPhone,
                    body = "Hmm, não entendi bem. Vou chamar um atendente para te ajudar! 😊"
                )
            )
        } else {
            whatsAppGateway.sendButtons(
                WhatsAppButtonMessage(
                    to = conversation.customerPhone,
                    body = "Não entendi bem. O que você deseja fazer?",
                    buttons = listOf(
                        ButtonOption("schedule", "Agendar horário"),
                        ButtonOption("cancel", "Cancelar agendamento"),
                        ButtonOption("human", "Falar com atendente")
                    )
                )
            )
        }
    }
}
