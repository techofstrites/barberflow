package com.barberflow.scheduling.application.command

import com.barberflow.core.tenant.TenantId
import com.barberflow.scheduling.domain.repository.AppointmentRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

data class ConfirmAppointmentCommand(val appointmentId: UUID, val tenantId: TenantId)

@Service
class ConfirmAppointmentUseCase(
    private val appointmentRepository: AppointmentRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    @Transactional
    fun execute(command: ConfirmAppointmentCommand) {
        val appointment = appointmentRepository.findById(command.appointmentId, command.tenantId)
            ?: throw NoSuchElementException("Appointment ${command.appointmentId} not found")

        appointment.confirm()
        appointmentRepository.save(appointment)
        appointment.domainEvents.forEach { eventPublisher.publishEvent(it) }
        appointment.clearEvents()
    }
}
