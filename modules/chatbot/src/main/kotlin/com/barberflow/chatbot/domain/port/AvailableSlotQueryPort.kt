package com.barberflow.chatbot.domain.port

import com.barberflow.core.tenant.TenantId
import java.time.ZonedDateTime
import java.util.UUID

data class AvailableSlotDto(
    val professionalId: UUID,
    val startAt: ZonedDateTime,
    val endAt: ZonedDateTime
)

interface AvailableSlotQueryPort {
    /** Returns the first [limit] available slots across the next [daysAhead] days for the professional. */
    fun findNextSlots(
        tenantId: TenantId,
        professionalId: UUID,
        daysAhead: Int = 7,
        limit: Int = 3,
        durationMinutes: Int = 30
    ): List<AvailableSlotDto>
}
