package com.barberflow.billing.domain.model

data class UsageMetrics(
    val messagesSentThisMonth: Int = 0,
    val activeAppointmentsThisMonth: Int = 0
) {
    fun incrementMessages() = copy(messagesSentThisMonth = messagesSentThisMonth + 1)
    fun incrementAppointments() = copy(activeAppointmentsThisMonth = activeAppointmentsThisMonth + 1)
    fun reset() = UsageMetrics()
}
