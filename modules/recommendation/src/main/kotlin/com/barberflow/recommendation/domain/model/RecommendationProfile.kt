package com.barberflow.recommendation.domain.model

import com.barberflow.core.domain.AggregateRoot
import com.barberflow.core.tenant.TenantId
import com.barberflow.recommendation.domain.event.RetentionWindowTriggered
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

data class LastRecommendation(
    val suggestedProfessionalId: UUID?,
    val suggestedSlot: String?,
    val suggestedServiceIds: List<UUID>,
    val generatedAt: LocalDate
)

class RecommendationProfile private constructor(
    val customerId: UUID,
    val tenantId: TenantId,
    var predictedNextVisitDate: LocalDate?,
    var confidenceScore: Float,
    var lastRecommendation: LastRecommendation?,
    var retentionWindow: RetentionWindow,
    var averageDaysBetweenVisits: Double,
    var lastAppointmentDate: LocalDate?
) : AggregateRoot() {

    companion object {
        fun create(
            customerId: UUID,
            tenantId: TenantId,
            retentionWindow: RetentionWindow = RetentionWindow.DEFAULT
        ) = RecommendationProfile(
            customerId = customerId,
            tenantId = tenantId,
            predictedNextVisitDate = null,
            confidenceScore = 0.0f,
            lastRecommendation = null,
            retentionWindow = retentionWindow,
            averageDaysBetweenVisits = 0.0,
            lastAppointmentDate = null
        )

        fun reconstitute(
            customerId: UUID, tenantId: TenantId, predictedNextVisitDate: LocalDate?,
            confidenceScore: Float, lastRecommendation: LastRecommendation?,
            retentionWindow: RetentionWindow, averageDaysBetweenVisits: Double,
            lastAppointmentDate: LocalDate?
        ) = RecommendationProfile(
            customerId, tenantId, predictedNextVisitDate, confidenceScore,
            lastRecommendation, retentionWindow, averageDaysBetweenVisits, lastAppointmentDate
        )
    }

    fun recordAppointment(date: LocalDate, totalAppointments: Int) {
        if (lastAppointmentDate != null && totalAppointments > 1) {
            val days = ChronoUnit.DAYS.between(lastAppointmentDate, date).toDouble()
            averageDaysBetweenVisits = if (averageDaysBetweenVisits == 0.0) days
            else (averageDaysBetweenVisits * (totalAppointments - 1) + days) / totalAppointments
        }
        lastAppointmentDate = date
        recalculatePrediction()
    }

    private fun recalculatePrediction() {
        if (averageDaysBetweenVisits <= 0 || lastAppointmentDate == null) {
            confidenceScore = 0.0f
            predictedNextVisitDate = null
            return
        }
        predictedNextVisitDate = lastAppointmentDate!!.plusDays(averageDaysBetweenVisits.toLong())
        // Confidence based on data points — higher avg stability = higher confidence
        confidenceScore = when {
            averageDaysBetweenVisits in 7.0..45.0 -> 0.8f
            averageDaysBetweenVisits in 5.0..60.0 -> 0.6f
            else -> 0.4f
        }
    }

    fun checkAndTriggerRetention(today: LocalDate) {
        val last = lastAppointmentDate ?: return
        val daysSince = ChronoUnit.DAYS.between(last, today)

        if (retentionWindow.isWithinWindow(daysSince) && confidenceScore >= 0.4f) {
            registerEvent(
                RetentionWindowTriggered(
                    customerId = customerId,
                    tenantId = tenantId,
                    daysSinceLastVisit = daysSince,
                    predictedNextVisitDate = predictedNextVisitDate,
                    suggestedProfessionalId = lastRecommendation?.suggestedProfessionalId,
                    suggestedServiceIds = lastRecommendation?.suggestedServiceIds ?: emptyList()
                )
            )
        }
    }
}
