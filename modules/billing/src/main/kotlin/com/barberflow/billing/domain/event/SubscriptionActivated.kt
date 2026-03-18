package com.barberflow.billing.domain.event

import com.barberflow.billing.domain.model.PlanFeature
import com.barberflow.billing.domain.model.PlanTier
import com.barberflow.core.domain.DomainEvent
import com.barberflow.core.tenant.TenantId
import java.time.Instant
import java.util.UUID

data class SubscriptionActivated(
    val subscriptionId: UUID,
    val tenantId: TenantId,
    val planTier: PlanTier,
    val features: Set<PlanFeature>,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val aggregateId: String = subscriptionId.toString()
) : DomainEvent
