package com.barberflow.chatbot.application.intent

import com.barberflow.chatbot.domain.model.ConversationState
import org.springframework.stereotype.Component

@Component
class IntentResolver(
    private val aiIntentResolver: AiIntentResolver
) {
    // Layer 2: keyword sets
    private val scheduleKeywords = setOf(
        "marcar", "agendar", "horário", "horario", "quero marcar",
        "quero agendar", "reservar", "marcar horário", "preciso de horário",
        "fazer agendamento", "quero um horário"
    )
    private val cancelKeywords = setOf(
        "cancelar", "desmarcar", "cancela", "não vou", "nao vou poder", "quero cancelar"
    )
    private val historyKeywords = setOf(
        "histórico", "historico", "meus agendamentos", "ver agendamentos",
        "agendamentos", "quero ver", "minhas reservas"
    )
    private val greetingKeywords = setOf(
        "oi", "olá", "ola", "bom dia", "boa tarde", "boa noite",
        "hey", "e aí", "e ai", "tudo bem", "oiee", "opa"
    )
    private val confirmKeywords = setOf(
        "sim", "confirmar", "confirma", "ok", "pode ser",
        "certo", "beleza", "perfeito", "tá bom", "ta bom", "yes", "quero"
    )
    private val declineKeywords = setOf(
        "não", "nao", "não quero", "agora não", "agora nao", "no", "cancela", "voltar"
    )

    fun resolve(message: String, currentState: ConversationState): Intent {
        val normalized = message.trim().lowercase()

        // Layer 1: exact button/list IDs — 100% deterministic
        if (normalized == "schedule") return Intent.ScheduleAppointment
        if (normalized == "cancel") return Intent.CancelAppointment
        if (normalized == "history") return Intent.ViewHistory
        if (normalized == "human") return Intent.Unrecognized

        if (normalized.startsWith("confirm:")) return Intent.Confirm
        if (normalized.startsWith("decline:")) return Intent.Decline
        if (normalized.startsWith("another_time:")) return Intent.AnotherTime
        if (normalized.startsWith("service:")) return Intent.SelectService(normalized.removePrefix("service:"))
        if (normalized.startsWith("professional:")) return Intent.SelectProfessional(normalized.removePrefix("professional:"))
        if (normalized.startsWith("day:")) return Intent.SelectDay(normalized.removePrefix("day:"))
        if (normalized.startsWith("slot:")) return Intent.SelectSlot(normalized.removePrefix("slot:"))
        if (normalized.startsWith("cancel_appt:")) return Intent.SelectCancelAppointment(normalized.removePrefix("cancel_appt:"))

        // Layer 2: keyword matching
        val keywordIntent = when (currentState) {
            ConversationState.AWAITING_CONFIRMATION -> resolveConfirmation(normalized)
            ConversationState.AWAITING_CANCEL_CONFIRMATION -> resolveConfirmation(normalized)
            ConversationState.IDLE, ConversationState.GREETING -> resolveInitial(normalized)
            else -> resolveGeneral(normalized)
        }
        if (keywordIntent !is Intent.Unrecognized) return keywordIntent

        // Layer 3: AI fallback (Claude Haiku)
        return aiIntentResolver.resolve(message, currentState)
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
            historyKeywords.any { message.contains(it) } -> Intent.ViewHistory
            else -> Intent.Unrecognized
        }

    private fun resolveGeneral(message: String): Intent =
        when {
            scheduleKeywords.any { message.contains(it) } -> Intent.ScheduleAppointment
            cancelKeywords.any { message.contains(it) } -> Intent.CancelAppointment
            historyKeywords.any { message.contains(it) } -> Intent.ViewHistory
            confirmKeywords.any { message.contains(it) } -> Intent.Confirm
            declineKeywords.any { message.contains(it) } -> Intent.Decline
            else -> Intent.Unrecognized
        }
}
