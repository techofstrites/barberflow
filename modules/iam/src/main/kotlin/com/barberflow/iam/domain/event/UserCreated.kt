package com.barberflow.iam.domain.event

import com.barberflow.core.domain.DomainEvent
import com.barberflow.core.tenant.TenantId
import com.barberflow.iam.domain.model.Role
import java.time.Instant
import java.util.UUID

data class UserCreated(
    val userId: UUID,
    val tenantId: TenantId,
    val email: String,
    val role: Role,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val aggregateId: String = userId.toString()
) : DomainEvent
