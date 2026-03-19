package com.barberflow.chatbot.domain.port

import com.barberflow.core.tenant.TenantId
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

data class AvailableSlotDto(
    val professionalId: UUID,
    val startAt: ZonedDateTime,
    val endAt: ZonedDateTime
)

interface AvailableSlotQueryPort {
    /** Returns the first [limit] available slots across the next [daysAhead] days. */
    fun findNextSlots(
        tenantId: TenantId,
        professionalId: UUID,
        daysAhead: Int = 7,
        limit: Int = 3,
        durationMinutes: Int = 30
    ): List<AvailableSlotDto>

    /** Returns dates that have at least one available slot in the next [daysAhead] days. */
    fun findAvailableDays(
        tenantId: TenantId,
        professionalId: UUID,
        daysAhead: Int = 14,
        durationMinutes: Int = 30
    ): List<LocalDate>

    /** Returns all available slots for a specific date. */
    fun findSlotsForDay(
        tenantId: TenantId,
        professionalId: UUID,
        date: LocalDate,
        durationMinutes: Int = 30
    ): List<AvailableSlotDto>
}
