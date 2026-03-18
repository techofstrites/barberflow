package com.barberflow.customer.domain.model

import com.barberflow.core.domain.AggregateRoot
import com.barberflow.core.tenant.TenantId
import com.barberflow.customer.domain.event.CustomerCreated
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class Customer private constructor(
    val id: UUID,
    val tenantId: TenantId,
    val phone: String,
    var name: String,
    var preferences: CustomerPreferences,
    var behaviorMetrics: BehaviorMetrics,
    var consentGiven: Boolean,
    val createdAt: Instant
) : AggregateRoot() {

    companion object {
        fun create(
            tenantId: TenantId,
            phone: String,
            name: String,
            consentGiven: Boolean = false
        ): Customer {
            require(phone.startsWith("+")) { "Phone must be in E.164 format (+5511999999999)" }
            val customer = Customer(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                phone = phone,
                name = name,
                preferences = CustomerPreferences(),
                behaviorMetrics = BehaviorMetrics(),
                consentGiven = consentGiven,
                createdAt = Instant.now()
            )
            customer.registerEvent(CustomerCreated(customer.id, tenantId, phone, name))
            return customer
        }

        fun reconstitute(
            id: UUID, tenantId: TenantId, phone: String, name: String,
            preferences: CustomerPreferences, behaviorMetrics: BehaviorMetrics,
            consentGiven: Boolean, createdAt: Instant
        ) = Customer(id, tenantId, phone, name, preferences, behaviorMetrics, consentGiven, createdAt)
    }

    fun recordAppointmentCompleted(date: LocalDate) {
        behaviorMetrics = behaviorMetrics.withNewAppointment(date)
    }

    fun recordNoShow() {
        behaviorMetrics = behaviorMetrics.withNoShow()
    }

    fun updatePreferences(prefs: CustomerPreferences) {
        preferences = prefs
    }

    fun giveConsent() {
        consentGiven = true
    }
}
