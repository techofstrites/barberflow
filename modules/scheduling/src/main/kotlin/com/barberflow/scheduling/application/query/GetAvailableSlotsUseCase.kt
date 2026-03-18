package com.barberflow.scheduling.application.query

import com.barberflow.scheduling.domain.model.AppointmentStatus
import com.barberflow.scheduling.domain.model.TimeSlot
import com.barberflow.scheduling.domain.repository.AppointmentRepository
import com.barberflow.scheduling.domain.repository.ScheduleRepository
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class GetAvailableSlotsUseCase(
    private val scheduleRepository: ScheduleRepository,
    private val appointmentRepository: AppointmentRepository
) {
    fun execute(query: GetAvailableSlotsQuery): List<TimeSlot> {
        val schedule = scheduleRepository.findByProfessionalId(query.tenantId, query.professionalId)
            ?: return emptyList()

        val workingHours = schedule.getWorkingHoursFor(query.date.dayOfWeek)
            ?: return emptyList()

        val zoneId = ZoneId.systemDefault()
        val dayStart = query.date.atTime(workingHours.startTime).atZone(zoneId)
        val dayEnd = query.date.atTime(workingHours.endTime).atZone(zoneId)

        val existingAppointments = appointmentRepository.findByTenantAndDateRange(
            query.tenantId,
            dayStart,
            dayEnd
        ).filter { it.professionalId == query.professionalId }
         .filter { it.status !in listOf(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW) }

        val slots = mutableListOf<TimeSlot>()
        var slotStart = dayStart

        while (slotStart.plusMinutes(query.serviceDurationMinutes.toLong()).isBefore(dayEnd) ||
               slotStart.plusMinutes(query.serviceDurationMinutes.toLong()) == dayEnd) {
            val slotEnd = slotStart.plusMinutes(query.serviceDurationMinutes.toLong())
            val candidate = TimeSlot(slotStart, slotEnd)

            val hasConflict = existingAppointments.any { appt ->
                TimeSlot(appt.timeSlot.startAt, appt.timeSlot.endAt).overlapsWith(candidate)
            }

            if (!hasConflict) {
                slots.add(candidate)
            }

            slotStart = slotStart.plusMinutes(workingHours.slotDurationMinutes.toLong())
        }

        return slots
    }
}
