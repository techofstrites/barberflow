package com.barberflow.chatbot.infrastructure.scheduling

import com.barberflow.chatbot.domain.port.AppointmentBookingPort
import com.barberflow.chatbot.domain.port.BookingRequest
import com.barberflow.chatbot.domain.port.CustomerResolverPort
import com.barberflow.scheduling.domain.model.Appointment
import com.barberflow.scheduling.domain.model.AppointmentStatus
import com.barberflow.scheduling.domain.model.ServiceItem
import com.barberflow.scheduling.domain.model.TimeSlot
import com.barberflow.scheduling.domain.repository.AppointmentRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Component
class AppointmentBookingAdapter(
    private val appointmentRepository: AppointmentRepository,
    private val customerResolverPort: CustomerResolverPort,
    private val eventPublisher: ApplicationEventPublisher
) : AppointmentBookingPort {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun book(request: BookingRequest): UUID {
        val customerId = customerResolverPort.findOrCreate(request.tenantId, request.customerPhone)

        val timeSlot = TimeSlot(
            startAt = request.startAt,
            endAt = request.startAt.plusMinutes(request.durationMinutes.toLong())
        )

        val conflicts = appointmentRepository.findByProfessionalAndTimeSlot(
            request.tenantId, request.professionalId, timeSlot
        ).filter { it.status !in listOf(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW) }

        require(conflicts.isEmpty()) { "Time slot is no longer available" }

        // Placeholder service while no service was selected in the chatbot flow
        val pendingService = ServiceItem(
            serviceId = UUID.nameUUIDFromBytes("chatbot-pending".toByteArray()),
            name = "A confirmar",
            price = BigDecimal.ZERO,
            durationMinutes = request.durationMinutes
        )

        val appointment = Appointment.schedule(
            tenantId = request.tenantId,
            customerId = customerId,
            professionalId = request.professionalId,
            services = listOf(pendingService),
            timeSlot = timeSlot
        )

        appointmentRepository.save(appointment)
        appointment.domainEvents.forEach { eventPublisher.publishEvent(it) }
        appointment.clearEvents()

        return appointment.id
    }
}
