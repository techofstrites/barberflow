package com.barberflow.chatbot.domain.model

import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

data class ConversationContext(
    val selectedServiceIds: List<UUID> = emptyList(),
    val selectedProfessionalId: UUID? = null,
    val selectedProfessionalName: String? = null,
    val selectedDate: LocalDate? = null,
    val selectedSlot: ZonedDateTime? = null,
    val selectedSlotEnd: ZonedDateTime? = null,
    val suggestedByAI: Boolean = false,
    val pendingAppointmentId: UUID? = null
)
