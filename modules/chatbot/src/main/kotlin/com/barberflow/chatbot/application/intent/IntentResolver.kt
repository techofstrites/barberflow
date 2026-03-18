package com.barberflow.chatbot.application.intent

import com.barberflow.chatbot.domain.model.ConversationState
import org.springframework.stereotype.Component

@Component
class IntentResolver {

    private val scheduleKeywords = setOf(
        "marcar", "agendar", "horário", "horario", "quero marcar",
        "quero agendar", "reservar", "marcar horário", "preciso de horário"
    )

    private val cancelKeywords = setOf(
        "cancelar", "desmarcar", "cancela", "não vou", "nao vou poder"
    )

    private val greetingKeywords = setOf(
        "oi", "olá", "ola", "bom dia", "boa tarde", "boa noite",
        "hey", "e aí", "e ai", "tudo bem", "oiee"
    )

    private val confirmKeywords = setOf(
        "sim", "confirmar", "confirma", "ok", "pode ser",
        "certo", "beleza", "perfeito", "tá bom", "ta bom", "yes"
    )

    private val declineKeywords = setOf(
        "não", "nao", "não quero", "agora não", "agora nao", "no"
    )

    fun resolve(message: String, currentState: ConversationState): Intent {
        val normalized = message.trim().lowercase()

        // Button/list IDs follow pattern: ACTION:ID
        if (normalized.startsWith("confirm:")) return Intent.Confirm
        if (normalized.startsWith("decline:")) return Intent.Decline
        if (normalized.startsWith("another_time:")) return Intent.AnotherTime
        if (normalized.startsWith("service:")) return Intent.SelectService(normalized.removePrefix("service:"))
        if (normalized.startsWith("professional:")) return Intent.SelectProfessional(normalized.removePrefix("professional:"))
        if (normalized.startsWith("slot:")) return Intent.SelectSlot(normalized.removePrefix("slot:"))

        // State-aware resolution
        return when (currentState) {
            ConversationState.AWAITING_CONFIRMATION -> resolveConfirmation(normalized)
            ConversationState.IDLE, ConversationState.GREETING -> resolveInitial(normalized)
            else -> resolveGeneral(normalized)
        }
    }

    private fun resolveConfirmation(message: String): Intent =
        when {
            confirmKeywords.any { message.contains(it) } -> Intent.Confirm
            declineKeywords.any { message.contains(it) } -> Intent.Decline
            else -> Intent.Unrecognized
        }

    private fun resolveInitial(message: String): Intent =
        when {
            greetingKeywords.any { message.contains(it) } -> Intent.Greeting
            scheduleKeywords.any { message.contains(it) } -> Intent.ScheduleAppointment
            cancelKeywords.any { message.contains(it) } -> Intent.CancelAppointment
            else -> Intent.Unrecognized
        }

    private fun resolveGeneral(message: String): Intent =
        when {
            scheduleKeywords.any { message.contains(it) } -> Intent.ScheduleAppointment
            cancelKeywords.any { message.contains(it) } -> Intent.CancelAppointment
            confirmKeywords.any { message.contains(it) } -> Intent.Confirm
            declineKeywords.any { message.contains(it) } -> Intent.Decline
            else -> Intent.Unrecognized
        }
}
