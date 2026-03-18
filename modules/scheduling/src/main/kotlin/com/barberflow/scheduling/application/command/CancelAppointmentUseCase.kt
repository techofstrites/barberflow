package com.barberflow.scheduling.application.command

import com.barberflow.scheduling.domain.repository.AppointmentRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CancelAppointmentUseCase(
    private val appointmentRepository: AppointmentRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    @Transactional
    fun execute(command: CancelAppointmentCommand) {
        val appointment = appointmentRepository.findById(command.appointmentId, command.tenantId)
            ?: throw NoSuchElementException("Appointment ${command.appointmentId} not found")

        appointment.cancel(command.reason)
        appointmentRepository.save(appointment)
        appointment.domainEvents.forEach { eventPublisher.publishEvent(it) }
        appointment.clearEvents()
    }
}
