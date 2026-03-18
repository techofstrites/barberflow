package com.barberflow.scheduling.infrastructure.persistence

import com.barberflow.core.tenant.TenantId
import com.barberflow.scheduling.domain.model.*
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "appointments")
class AppointmentEntity(
    @Id
    val id: UUID,

    @Column(name = "tenant_id", nullable = false)
    val tenantId: UUID,

    @Column(name = "customer_id", nullable = false)
    val customerId: UUID,

    @Column(name = "professional_id", nullable = false)
    val professionalId: UUID,

    @Column(name = "start_at", nullable = false)
    val startAt: Instant,

    @Column(name = "end_at", nullable = false)
    val endAt: Instant,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: AppointmentStatus = AppointmentStatus.PENDING_CONFIRMATION,

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,

    @Column(name = "suggested_by_ai", nullable = false)
    val suggestedByAI: Boolean = false,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "appointment", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val services: MutableList<AppointmentServiceEntity> = mutableListOf()
) {
    fun toDomain(): Appointment {
        val timeSlot = TimeSlot(
            startAt = startAt.atZone(ZoneOffset.UTC),
            endAt = endAt.atZone(ZoneOffset.UTC)
        )
        return Appointment.reconstitute(
            id = id,
            tenantId = TenantId(tenantId),
            customerId = customerId,
            professionalId = professionalId,
            services = services.map { it.toDomain() },
            timeSlot = timeSlot,
            status = status,
            paymentStatus = paymentStatus,
            createdAt = createdAt,
            suggestedByAI = suggestedByAI
        )
    }

    companion object {
        fun fromDomain(appointment: Appointment): AppointmentEntity {
            val entity = AppointmentEntity(
                id = appointment.id,
                tenantId = appointment.tenantId.value,
                customerId = appointment.customerId,
                professionalId = appointment.professionalId,
                startAt = appointment.timeSlot.startAt.toInstant(),
                endAt = appointment.timeSlot.endAt.toInstant(),
                status = appointment.status,
                paymentStatus = appointment.paymentStatus,
                suggestedByAI = appointment.suggestedByAI,
                createdAt = appointment.createdAt
            )
            appointment.services.forEach { service ->
                entity.services.add(AppointmentServiceEntity.fromDomain(entity, service))
            }
            return entity
        }
    }
}

@Entity
@Table(name = "appointment_services")
class AppointmentServiceEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    val appointment: AppointmentEntity,

    @Column(name = "service_id", nullable = false)
    val serviceId: UUID,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false, precision = 10, scale = 2)
    val price: BigDecimal,

    @Column(name = "duration_minutes", nullable = false)
    val durationMinutes: Int
) {
    fun toDomain() = ServiceItem(
        serviceId = serviceId,
        name = name,
        price = price,
        durationMinutes = durationMinutes
    )

    companion object {
        fun fromDomain(appointment: AppointmentEntity, service: ServiceItem) =
            AppointmentServiceEntity(
                appointment = appointment,
                serviceId = service.serviceId,
                name = service.name,
                price = service.price,
                durationMinutes = service.durationMinutes
            )
    }
}
