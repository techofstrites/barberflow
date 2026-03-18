package com.barberflow.chatbot.infrastructure.persistence

import com.barberflow.chatbot.domain.model.*
import com.barberflow.core.tenant.TenantId
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "conversations")
class ConversationEntity(
    @Id
    val id: UUID,

    @Column(name = "tenant_id", nullable = false)
    val tenantId: UUID,

    @Column(name = "customer_phone", nullable = false)
    val customerPhone: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val state: ConversationState,

    @Column(columnDefinition = "TEXT")
    val context: String, // JSON serialized ConversationContext

    @Column(name = "message_history", columnDefinition = "TEXT")
    val messageHistory: String, // JSON serialized list

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant,

    @Column(name = "unrecognized_count", nullable = false)
    val unrecognizedCount: Int = 0
) {
    fun toDomain(mapper: ObjectMapper): Conversation {
        return Conversation.reconstitute(
            id = id,
            tenantId = TenantId(tenantId),
            customerPhone = customerPhone,
            state = state,
            context = mapper.readValue(context),
            messageHistory = mapper.readValue<List<ConversationMessage>>(messageHistory).toMutableList(),
            createdAt = createdAt,
            expiresAt = expiresAt,
            unrecognizedCount = unrecognizedCount
        )
    }

    companion object {
        fun fromDomain(conversation: Conversation, mapper: ObjectMapper) = ConversationEntity(
            id = conversation.id,
            tenantId = conversation.tenantId.value,
            customerPhone = conversation.customerPhone,
            state = conversation.state,
            context = mapper.writeValueAsString(conversation.context),
            messageHistory = mapper.writeValueAsString(conversation.messageHistory),
            createdAt = conversation.createdAt,
            expiresAt = conversation.expiresAt,
            unrecognizedCount = conversation.unrecognizedCount
        )
    }
}
