package com.barberflow.scheduling.application.command

import com.barberflow.scheduling.domain.model.AppointmentStatus
import com.barberflow.scheduling.domain.repository.AppointmentRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CheckInUseCase(
    private val appointmentRepository: AppointmentRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    @Transactional
    fun execute(command: CheckInCommand) {
        val appointment = appointmentRepository.findById(command.appointmentId, command.tenantId)
            ?: throw NoSuchElementException("Appointment ${command.appointmentId} not found")

        require(appointment.status == AppointmentStatus.CONFIRMED) {
            "Only CONFIRMED appointments can be checked in"
        }

        appointment.status = AppointmentStatus.IN_PROGRESS
        appointmentRepository.save(appointment)
        appointment.domainEvents.forEach { eventPublisher.publishEvent(it) }
        appointment.clearEvents()
    }
}
