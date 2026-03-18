package com.barberflow.customer.domain.event

import com.barberflow.core.domain.DomainEvent
import com.barberflow.core.tenant.TenantId
import java.time.Instant
import java.util.UUID

data class CustomerCreated(
    val customerId: UUID,
    val tenantId: TenantId,
    val phone: String,
    val name: String,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val aggregateId: String = customerId.toString()
) : DomainEvent
