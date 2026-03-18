package com.barberflow.chatbot.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ConversationJpaRepository : JpaRepository<ConversationEntity, UUID> {
    fun findTopByCustomerPhoneAndTenantIdOrderByCreatedAtDesc(
        customerPhone: String,
        tenantId: UUID
    ): ConversationEntity?
}
