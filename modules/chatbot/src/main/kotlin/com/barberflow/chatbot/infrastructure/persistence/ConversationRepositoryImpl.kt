package com.barberflow.chatbot.infrastructure.persistence

import com.barberflow.chatbot.domain.model.Conversation
import com.barberflow.chatbot.domain.repository.ConversationRepository
import com.barberflow.core.tenant.TenantId
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Repository

@Repository
class ConversationRepositoryImpl(
    private val jpa: ConversationJpaRepository,
    private val mapper: ObjectMapper
) : ConversationRepository {

    override fun save(conversation: Conversation): Conversation {
        jpa.save(ConversationEntity.fromDomain(conversation, mapper))
        return conversation
    }

    override fun findActiveByPhone(phone: String, tenantId: TenantId): Conversation? =
        jpa.findTopByCustomerPhoneAndTenantIdOrderByCreatedAtDesc(phone, tenantId.value)
            ?.toDomain(mapper)

    override fun findById(id: String): Conversation? =
        jpa.findById(java.util.UUID.fromString(id)).orElse(null)?.toDomain(mapper)
}
