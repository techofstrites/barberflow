package com.barberflow.chatbot.infrastructure.scheduling

import com.barberflow.chatbot.domain.port.AppointmentBookingPort
import com.barberflow.chatbot.domain.port.BookingRequest
import com.barberflow.chatbot.domain.port.CustomerResolverPort
import com.barberflow.scheduling.domain.model.Appointment
import com.barberflow.scheduling.domain.model.AppointmentStatus
import com.barberflow.scheduling.domain.model.ServiceItem
import com.barberflow.scheduling.domain.model.TimeSlot
import com.barberflow.scheduling.domain.repository.AppointmentRepository
import org.slf4j.LoggerFactory
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

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun book(request: BookingRequest): UUID {
        log.info("Booking: phone={} professional={} startAt={}", request.customerPhone, request.professionalId, request.startAt)

        log.debug("Booking step 1: findOrCreate customer")
        val customerId = customerResolverPort.findOrCreate(request.tenantId, request.customerPhone)
        log.debug("Booking step 1 OK: customerId={}", customerId)

        log.debug("Booking step 2: build timeSlot startAt={} +{}min", request.startAt, request.durationMinutes)
        val timeSlot = TimeSlot(
            startAt = request.startAt,
            endAt = request.startAt.plusMinutes(request.durationMinutes.toLong())
        )
        log.debug("Booking step 2 OK: endAt={}", timeSlot.endAt)

        log.debug("Booking step 3: conflict check")
        val conflicts = appointmentRepository.findByProfessionalAndTimeSlot(
            request.tenantId, request.professionalId, timeSlot
        ).filter { it.status !in listOf(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW) }
        log.debug("Booking step 3 OK: conflicts={}", conflicts.size)

        require(conflicts.isEmpty()) { "Time slot is no longer available" }

        log.debug("Booking step 4: create appointment")
        val pendingService = if (request.serviceId != null && request.serviceName != null) {
            ServiceItem(
                serviceId = request.serviceId,
                name = request.serviceName,
                price = request.servicePrice ?: BigDecimal.ZERO,
                durationMinutes = request.durationMinutes
            )
        } else {
            ServiceItem(
                serviceId = UUID.nameUUIDFromBytes("chatbot-pending".toByteArray()),
                name = "A confirmar",
                price = BigDecimal.ZERO,
                durationMinutes = request.durationMinutes
            )
        }

        val appointment = Appointment.schedule(
            tenantId = request.tenantId,
            customerId = customerId,
            professionalId = request.professionalId,
            services = listOf(pendingService),
            timeSlot = timeSlot
        )
        log.debug("Booking step 4 OK: appointmentId={}", appointment.id)

        log.debug("Booking step 5: save appointment")
        appointmentRepository.save(appointment)
        log.debug("Booking step 5 OK")

        log.debug("Booking step 6: publish events")
        appointment.domainEvents.forEach { eventPublisher.publishEvent(it) }
        appointment.clearEvents()
        log.debug("Booking step 6 OK")

        log.info("Booking completed: appointmentId={}", appointment.id)
        return appointment.id
    }
}
