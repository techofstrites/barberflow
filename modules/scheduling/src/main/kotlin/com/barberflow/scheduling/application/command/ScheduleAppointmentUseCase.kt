package com.barberflow.scheduling.application.command

import com.barberflow.scheduling.domain.model.Appointment
import com.barberflow.scheduling.domain.model.AppointmentStatus
import com.barberflow.scheduling.domain.model.ServiceItem
import com.barberflow.scheduling.domain.model.TimeSlot
import com.barberflow.scheduling.domain.repository.AppointmentRepository
import com.barberflow.scheduling.domain.repository.ServiceCatalogRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ScheduleAppointmentUseCase(
    private val appointmentRepository: AppointmentRepository,
    private val serviceCatalogRepository: ServiceCatalogRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    @Transactional
    fun execute(command: ScheduleAppointmentCommand): UUID {
        val services = command.serviceIds.map {
            serviceCatalogRepository.findById(command.tenantId, it)
                ?: throw IllegalArgumentException("Service $it not found")
        }

        val totalDuration = services.sumOf { it.durationMinutes }
        val timeSlot = TimeSlot(
            startAt = command.startAt,
            endAt = command.startAt.plusMinutes(totalDuration.toLong())
        )

        val conflicts = appointmentRepository.findByProfessionalAndTimeSlot(
            command.tenantId, command.professionalId, timeSlot
        ).filter { it.status !in listOf(
            AppointmentStatus.CANCELLED,
            AppointmentStatus.NO_SHOW
        )}

        require(conflicts.isEmpty()) { "Time slot is not available" }

        val appointment = Appointment.schedule(
            tenantId = command.tenantId,
            customerId = command.customerId,
            professionalId = command.professionalId,
            services = services,
            timeSlot = timeSlot,
            suggestedByAI = command.suggestedByAI
        )

        appointmentRepository.save(appointment)
        appointment.domainEvents.forEach { eventPublisher.publishEvent(it) }
        appointment.clearEvents()

        return appointment.id
    }
}
