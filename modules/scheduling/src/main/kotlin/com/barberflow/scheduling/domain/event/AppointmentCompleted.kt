package com.barberflow.scheduling.domain.event

import com.barberflow.core.domain.DomainEvent
import com.barberflow.core.tenant.TenantId
import com.barberflow.scheduling.domain.model.ServiceItem
import java.time.Instant
import java.util.UUID

data class AppointmentCompleted(
    val appointmentId: UUID,
    val tenantId: TenantId,
    val customerId: UUID,
    val professionalId: UUID,
    val services: List<ServiceItem>,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val aggregateId: String = appointmentId.toString()
) : DomainEvent
