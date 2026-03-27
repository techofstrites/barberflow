package com.barberflow.chatbot.domain.repository

import com.barberflow.chatbot.domain.model.Conversation
import com.barberflow.core.tenant.TenantId

interface ConversationRepository {
    fun save(conversation: Conversation): Conversation
    fun findActiveByPhone(phone: String, tenantId: TenantId): Conversation?
    fun findById(id: String): Conversation?
    fun deleteByPhone(phone: String)
}
