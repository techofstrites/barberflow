package com.barberflow.customer.domain.model

import java.time.LocalDate

data class BehaviorMetrics(
    val lastAppointmentDate: LocalDate? = null,
    val averageDaysBetweenVisits: Double = 0.0,
    val totalAppointments: Int = 0,
    val noShowCount: Int = 0
) {
    fun withNewAppointment(date: LocalDate): BehaviorMetrics {
        val newTotal = totalAppointments + 1
        val newAvg = if (lastAppointmentDate != null && totalAppointments > 0) {
            val days = java.time.temporal.ChronoUnit.DAYS.between(lastAppointmentDate, date).toDouble()
            (averageDaysBetweenVisits * (newTotal - 1) + days) / newTotal
        } else averageDaysBetweenVisits
        return copy(
            lastAppointmentDate = date,
            averageDaysBetweenVisits = newAvg,
            totalAppointments = newTotal
        )
    }

    fun withNoShow(): BehaviorMetrics = copy(noShowCount = noShowCount + 1)
}
