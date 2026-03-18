package com.barberflow.chatbot.domain.event

import com.barberflow.core.domain.DomainEvent
import com.barberflow.core.tenant.TenantId
import java.time.Instant
import java.util.UUID

data class MessageReceived(
    val conversationId: UUID,
    val tenantId: TenantId,
    val customerPhone: String,
    val content: String,
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    override val aggregateId: String = conversationId.toString()
) : DomainEvent
