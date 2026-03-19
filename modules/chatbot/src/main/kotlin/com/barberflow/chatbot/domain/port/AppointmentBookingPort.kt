package com.barberflow.chatbot.domain.port

import com.barberflow.core.tenant.TenantId
import java.time.ZonedDateTime
import java.util.UUID

data class BookingRequest(
    val tenantId: TenantId,
    val customerPhone: String,
    val professionalId: UUID,
    val startAt: ZonedDateTime,
    val durationMinutes: Int = 30
)

interface AppointmentBookingPort {
    fun book(request: BookingRequest): UUID
}
