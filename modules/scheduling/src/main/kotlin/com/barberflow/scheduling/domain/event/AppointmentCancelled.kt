package com.barberflow.scheduling.domain.event

import com.barberflow.core.domain.DomainEvent
import com.barberflow.core.tenant.TenantId
import java.time.Instant
import java.util.UUID

data class AppointmentCancelled(
    val appointmentId: UUID,
    val tenantId: TenantId,
    val customerId: UUID,
    val reason: String?,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val aggregateId: String = appointmentId.toString()
) : DomainEvent
