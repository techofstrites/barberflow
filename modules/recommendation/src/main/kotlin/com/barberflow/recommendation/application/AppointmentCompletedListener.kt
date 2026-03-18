package com.barberflow.recommendation.application

import com.barberflow.core.tenant.TenantId
import com.barberflow.recommendation.domain.model.RecommendationProfile
import com.barberflow.recommendation.domain.model.RetentionWindow
import com.barberflow.recommendation.domain.repository.RecommendationProfileRepository
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID

data class AppointmentCompletedPayload(
    val appointmentId: UUID,
    val tenantId: String,
    val customerId: UUID,
    val professionalId: UUID,
    val completedAt: LocalDate = LocalDate.now(),
    val totalAppointments: Int = 1
)

@Component
class AppointmentCompletedListener(
    private val profileRepository: RecommendationProfileRepository,
    private val retentionCheckJob: RetentionCheckJob
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    fun onAppointmentCompleted(payload: AppointmentCompletedPayload) {
        val tenantId = TenantId.from(payload.tenantId)
        val profile = profileRepository.findByCustomerId(payload.customerId, tenantId)
            ?: RecommendationProfile.create(payload.customerId, tenantId)

        profile.recordAppointment(payload.completedAt, payload.totalAppointments)
        profileRepository.save(profile)
        retentionCheckJob.registerTenant(payload.tenantId)

        log.debug("Updated recommendation profile for customer {}", payload.customerId)
    }
}
