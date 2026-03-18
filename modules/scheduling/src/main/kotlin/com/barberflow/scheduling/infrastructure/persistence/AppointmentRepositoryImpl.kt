package com.barberflow.scheduling.infrastructure.persistence

import com.barberflow.core.tenant.TenantId
import com.barberflow.scheduling.domain.model.Appointment
import com.barberflow.scheduling.domain.model.AppointmentStatus
import com.barberflow.scheduling.domain.model.TimeSlot
import com.barberflow.scheduling.domain.repository.AppointmentRepository
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime
import java.util.UUID

@Repository
class AppointmentRepositoryImpl(
    private val jpa: AppointmentJpaRepository
) : AppointmentRepository {

    override fun save(appointment: Appointment): Appointment {
        jpa.save(AppointmentEntity.fromDomain(appointment))
        return appointment
    }

    override fun findById(id: UUID, tenantId: TenantId): Appointment? =
        jpa.findByIdAndTenantId(id, tenantId.value)?.toDomain()

    override fun findByProfessionalAndTimeSlot(
        tenantId: TenantId,
        professionalId: UUID,
        timeSlot: TimeSlot
    ): List<Appointment> =
        jpa.findOverlapping(
            tenantId.value,
            professionalId,
            timeSlot.startAt.toInstant(),
            timeSlot.endAt.toInstant()
        ).map { it.toDomain() }

    override fun findConfirmedBefore(startAt: ZonedDateTime): List<Appointment> =
        jpa.findByStatusAndStartAtBefore(AppointmentStatus.CONFIRMED, startAt.toInstant())
            .map { it.toDomain() }

    override fun findByCustomerId(tenantId: TenantId, customerId: UUID): List<Appointment> =
        jpa.findByTenantIdAndCustomerId(tenantId.value, customerId).map { it.toDomain() }

    override fun findByTenantAndDateRange(
        tenantId: TenantId,
        from: ZonedDateTime,
        to: ZonedDateTime
    ): List<Appointment> =
        jpa.findByTenantIdAndDateRange(tenantId.value, from.toInstant(), to.toInstant())
            .map { it.toDomain() }
}
