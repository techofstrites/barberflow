package com.barberflow.scheduling.domain.repository

import com.barberflow.core.tenant.TenantId
import com.barberflow.scheduling.domain.model.Appointment
import com.barberflow.scheduling.domain.model.AppointmentStatus
import com.barberflow.scheduling.domain.model.TimeSlot
import java.time.ZonedDateTime
import java.util.UUID

interface AppointmentRepository {
    fun save(appointment: Appointment): Appointment
    fun findById(id: UUID, tenantId: TenantId): Appointment?
    fun findByProfessionalAndTimeSlot(
        tenantId: TenantId,
        professionalId: UUID,
        timeSlot: TimeSlot
    ): List<Appointment>
    fun findConfirmedBefore(startAt: ZonedDateTime): List<Appointment>
    fun findByCustomerId(tenantId: TenantId, customerId: UUID): List<Appointment>
    fun findByTenantAndDateRange(
        tenantId: TenantId,
        from: ZonedDateTime,
        to: ZonedDateTime
    ): List<Appointment>
}
