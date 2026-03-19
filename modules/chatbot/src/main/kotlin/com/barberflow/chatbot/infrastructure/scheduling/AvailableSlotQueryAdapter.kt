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
}
