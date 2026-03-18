package com.barberflow.customer.infrastructure.persistence

import com.barberflow.core.tenant.TenantId
import com.barberflow.customer.domain.model.*
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "customers")
class CustomerEntity(
    @Id
    val id: UUID,

    @Column(name = "tenant_id", nullable = false)
    val tenantId: UUID,

    @Column(nullable = false)
    val phone: String,

    @Column(nullable = false)
    val name: String,

    @Column(name = "preferred_professional_id")
    val preferredProfessionalId: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_time_of_day")
    val preferredTimeOfDay: TimeOfDay? = null,

    @Column(name = "last_appointment_date")
    val lastAppointmentDate: LocalDate? = null,

    @Column(name = "avg_days_between_visits", nullable = false)
    val avgDaysBetweenVisits: Double = 0.0,

    @Column(name = "total_appointments", nullable = false)
    val totalAppointments: Int = 0,

    @Column(name = "no_show_count", nullable = false)
    val noShowCount: Int = 0,

    @Column(name = "consent_given", nullable = false)
    val consentGiven: Boolean = false,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
) {
    fun toDomain() = Customer.reconstitute(
        id = id,
        tenantId = TenantId(tenantId),
        phone = phone,
        name = name,
        preferences = CustomerPreferences(
            preferredProfessionalId = preferredProfessionalId,
            preferredTimeOfDay = preferredTimeOfDay
        ),
        behaviorMetrics = BehaviorMetrics(
            lastAppointmentDate = lastAppointmentDate,
            averageDaysBetweenVisits = avgDaysBetweenVisits,
            totalAppointments = totalAppointments,
            noShowCount = noShowCount
        ),
        consentGiven = consentGiven,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(customer: Customer) = CustomerEntity(
            id = customer.id,
            tenantId = customer.tenantId.value,
            phone = customer.phone,
            name = customer.name,
            preferredProfessionalId = customer.preferences.preferredProfessionalId,
            preferredTimeOfDay = customer.preferences.preferredTimeOfDay,
            lastAppointmentDate = customer.behaviorMetrics.lastAppointmentDate,
            avgDaysBetweenVisits = customer.behaviorMetrics.averageDaysBetweenVisits,
            totalAppointments = customer.behaviorMetrics.totalAppointments,
            noShowCount = customer.behaviorMetrics.noShowCount,
            consentGiven = customer.consentGiven,
            createdAt = customer.createdAt
        )
    }
}
