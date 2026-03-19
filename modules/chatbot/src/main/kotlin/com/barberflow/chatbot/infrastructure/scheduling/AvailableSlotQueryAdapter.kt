package com.barberflow.chatbot.infrastructure.scheduling

import com.barberflow.chatbot.domain.port.AvailableSlotDto
import com.barberflow.chatbot.domain.port.AvailableSlotQueryPort
import com.barberflow.core.tenant.TenantId
import com.barberflow.scheduling.application.query.GetAvailableSlotsQuery
import com.barberflow.scheduling.application.query.GetAvailableSlotsUseCase
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

@Component
class AvailableSlotQueryAdapter(
    private val getAvailableSlotsUseCase: GetAvailableSlotsUseCase
) : AvailableSlotQueryPort {

    override fun findNextSlots(
        tenantId: TenantId,
        professionalId: UUID,
        daysAhead: Int,
        limit: Int,
        durationMinutes: Int
    ): List<AvailableSlotDto> {
        val today = LocalDate.now()
        val now = ZonedDateTime.now()
        val result = mutableListOf<AvailableSlotDto>()

        for (dayOffset in 0 until daysAhead) {
            if (result.size >= limit) break

            val date = today.plusDays(dayOffset.toLong())
            val slots = getAvailableSlotsUseCase.execute(
                GetAvailableSlotsQuery(
                    tenantId = tenantId,
                    professionalId = professionalId,
                    date = date,
                    serviceDurationMinutes = durationMinutes
                )
            )

            for (slot in slots) {
                if (result.size >= limit) break
                // Skip slots that have already started
                if (slot.startAt.isAfter(now)) {
                    result.add(AvailableSlotDto(professionalId, slot.startAt, slot.endAt))
                }
            }
        }

        return result
    }

    override fun findAvailableDays(
        tenantId: TenantId,
        professionalId: UUID,
        daysAhead: Int,
        durationMinutes: Int
    ): List<LocalDate> {
        val today = LocalDate.now()
        val now = ZonedDateTime.now()
        val days = mutableListOf<LocalDate>()

        for (dayOffset in 0 until daysAhead) {
            val date = today.plusDays(dayOffset.toLong())
            val slots = getAvailableSlotsUseCase.execute(
                GetAvailableSlotsQuery(tenantId, professionalId, date, durationMinutes)
            )
            val hasFutureSlot = slots.any { it.startAt.isAfter(now) }
            if (hasFutureSlot) days.add(date)
        }

        return days
    }

    override fun findSlotsForDay(
        tenantId: TenantId,
        professionalId: UUID,
        date: LocalDate,
        durationMinutes: Int
    ): List<AvailableSlotDto> {
        val now = ZonedDateTime.now()
        return getAvailableSlotsUseCase.execute(
            GetAvailableSlotsQuery(tenantId, professionalId, date, durationMinutes)
        )
            .filter { it.startAt.isAfter(now) }
            .map { AvailableSlotDto(professionalId, it.startAt, it.endAt) }
    }
}
