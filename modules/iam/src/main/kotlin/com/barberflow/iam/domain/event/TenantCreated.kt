package com.barberflow.iam.domain.event

import com.barberflow.core.domain.DomainEvent
import com.barberflow.core.tenant.TenantId
import java.time.Instant
import java.util.UUID

data class TenantCreated(
    val tenantId: TenantId,
    val slug: String,
    val name: String,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val aggregateId: String = tenantId.toString()
) : DomainEvent
