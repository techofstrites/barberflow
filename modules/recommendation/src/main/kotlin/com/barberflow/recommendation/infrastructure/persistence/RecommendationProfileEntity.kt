package com.barberflow.recommendation.infrastructure.persistence

import com.barberflow.core.tenant.TenantId
import com.barberflow.recommendation.domain.model.LastRecommendation
import com.barberflow.recommendation.domain.model.RecommendationProfile
import com.barberflow.recommendation.domain.model.RetentionWindow
import jakarta.persistence.*
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "recommendation_profiles")
class RecommendationProfileEntity(
    @Id
    @Column(name = "customer_id")
    val customerId: UUID,

    @Column(name = "tenant_id", nullable = false)
    val tenantId: UUID,

    @Column(name = "predicted_next_visit")
    val predictedNextVisitDate: LocalDate? = null,

    @Column(name = "confidence_score", nullable = false)
    val confidenceScore: Float = 0.0f,

    @Column(name = "last_appointment_date")
    val lastAppointmentDate: LocalDate? = null,

    @Column(name = "avg_days_between_visits", nullable = false)
    val avgDaysBetweenVisits: Double = 0.0,

    @Column(name = "retention_min_days", nullable = false)
    val retentionMinDays: Int = 20,

    @Column(name = "retention_max_days", nullable = false)
    val retentionMaxDays: Int = 30,

    @Column(name = "suggested_professional_id")
    val suggestedProfessionalId: UUID? = null
) {
    fun toDomain() = RecommendationProfile.reconstitute(
        customerId = customerId,
        tenantId = TenantId(tenantId),
        predictedNextVisitDate = predictedNextVisitDate,
        confidenceScore = confidenceScore,
        lastRecommendation = if (suggestedProfessionalId != null)
            LastRecommendation(suggestedProfessionalId, null, emptyList(), LocalDate.now())
        else null,
        retentionWindow = RetentionWindow(retentionMinDays, retentionMaxDays),
        averageDaysBetweenVisits = avgDaysBetweenVisits,
        lastAppointmentDate = lastAppointmentDate
    )

    companion object {
        fun fromDomain(p: RecommendationProfile) = RecommendationProfileEntity(
            customerId = p.customerId,
            tenantId = p.tenantId.value,
            predictedNextVisitDate = p.predictedNextVisitDate,
            confidenceScore = p.confidenceScore,
            lastAppointmentDate = p.lastAppointmentDate,
            avgDaysBetweenVisits = p.averageDaysBetweenVisits,
            retentionMinDays = p.retentionWindow.minDays,
            retentionMaxDays = p.retentionWindow.maxDays,
            suggestedProfessionalId = p.lastRecommendation?.suggestedProfessionalId
        )
    }
}
