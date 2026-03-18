package com.barberflow.billing.domain.event

import com.barberflow.core.domain.DomainEvent
import com.barberflow.core.tenant.TenantId
import java.time.Instant
import java.util.UUID

data class SubscriptionCancelled(
    val subscriptionId: UUID,
    val tenantId: TenantId,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val aggregateId: String = subscriptionId.toString()
) : DomainEvent
