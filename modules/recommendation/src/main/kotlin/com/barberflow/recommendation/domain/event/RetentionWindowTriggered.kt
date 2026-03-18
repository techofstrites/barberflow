package com.barberflow.recommendation.domain.event

import com.barberflow.core.domain.DomainEvent
import com.barberflow.core.tenant.TenantId
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class RetentionWindowTriggered(
    val customerId: UUID,
    val tenantId: TenantId,
    val daysSinceLastVisit: Long,
    val predictedNextVisitDate: LocalDate?,
    val suggestedProfessionalId: UUID?,
    val suggestedServiceIds: List<UUID>,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val aggregateId: String = customerId.toString()
) : DomainEvent
