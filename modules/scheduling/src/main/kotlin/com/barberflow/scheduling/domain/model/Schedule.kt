package com.barberflow.scheduling.domain.model

import com.barberflow.core.tenant.TenantId
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

data class WorkingHours(
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val slotDurationMinutes: Int = 30
)

data class Schedule(
    val id: UUID,
    val tenantId: TenantId,
    val professionalId: UUID,
    val workingHours: List<WorkingHours>
) {
    fun isAvailableOn(dayOfWeek: DayOfWeek): Boolean =
        workingHours.any { it.dayOfWeek == dayOfWeek }

    fun getWorkingHoursFor(dayOfWeek: DayOfWeek): WorkingHours? =
        workingHours.firstOrNull { it.dayOfWeek == dayOfWeek }
}
