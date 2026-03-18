package com.barberflow.scheduling.infrastructure.scheduler

import com.barberflow.scheduling.domain.repository.AppointmentRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@Component
class NoShowDetectionJob(
    private val appointmentRepository: AppointmentRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 300_000) // every 5 minutes
    @Transactional
    fun detectNoShows() {
        val threshold = ZonedDateTime.now().minusHours(2)
        val candidates = appointmentRepository.findConfirmedBefore(threshold)

        candidates.forEach { appointment ->
            try {
                appointment.markNoShow()
                appointmentRepository.save(appointment)
                appointment.domainEvents.forEach { eventPublisher.publishEvent(it) }
                appointment.clearEvents()
                log.info("Marked appointment ${appointment.id} as NO_SHOW")
            } catch (e: Exception) {
                log.error("Failed to mark appointment ${appointment.id} as NO_SHOW", e)
            }
        }
    }
}
