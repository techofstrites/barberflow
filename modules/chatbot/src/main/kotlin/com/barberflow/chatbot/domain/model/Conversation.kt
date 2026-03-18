package com.barberflow.chatbot.domain.model

import com.barberflow.core.domain.AggregateRoot
import com.barberflow.core.tenant.TenantId
import com.barberflow.chatbot.domain.event.ConversationExpired
import com.barberflow.chatbot.domain.event.MessageReceived
import java.time.Instant
import java.util.UUID

class Conversation private constructor(
    val id: UUID,
    val tenantId: TenantId,
    val customerPhone: String,
    var state: ConversationState,
    var context: ConversationContext,
    val messageHistory: MutableList<ConversationMessage>,
    val createdAt: Instant,
    var expiresAt: Instant,
    var unrecognizedCount: Int = 0
) : AggregateRoot() {

    companion object {
        private val SESSION_TTL_HOURS = 24L
        private val UNRECOGNIZED_THRESHOLD = 3

        fun start(tenantId: TenantId, customerPhone: String): Conversation {
            return Conversation(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                customerPhone = customerPhone,
                state = ConversationState.GREETING,
                context = ConversationContext(),
                messageHistory = mutableListOf(),
                createdAt = Instant.now(),
                expiresAt = Instant.now().plusSeconds(SESSION_TTL_HOURS * 3600)
            )
        }

        fun reconstitute(
            id: UUID, tenantId: TenantId, customerPhone: String,
            state: ConversationState, context: ConversationContext,
            messageHistory: MutableList<ConversationMessage>,
            createdAt: Instant, expiresAt: Instant, unrecognizedCount: Int
        ) = Conversation(id, tenantId, customerPhone, state, context, messageHistory, createdAt, expiresAt, unrecognizedCount)
    }

    fun addInboundMessage(content: String) {
        messageHistory.add(ConversationMessage(MessageDirection.INBOUND, content))
        registerEvent(MessageReceived(id, tenantId, customerPhone, content))
    }

    fun addOutboundMessage(content: String) {
        messageHistory.add(ConversationMessage(MessageDirection.OUTBOUND, content))
    }

    fun transitionTo(newState: ConversationState) {
        state = newState
    }

    fun updateContext(updater: (ConversationContext) -> ConversationContext) {
        context = updater(context)
    }

    fun incrementUnrecognized() {
        unrecognizedCount++
    }

    fun resetUnrecognized() {
        unrecognizedCount = 0
    }

    fun shouldTransferToHuman(): Boolean = unrecognizedCount >= UNRECOGNIZED_THRESHOLD

    fun isExpired(): Boolean = Instant.now().isAfter(expiresAt)

    fun expire() {
        state = ConversationState.IDLE
        registerEvent(ConversationExpired(id, tenantId, customerPhone))
    }

    fun reset() {
        state = ConversationState.GREETING
        context = ConversationContext()
        unrecognizedCount = 0
        expiresAt = Instant.now().plusSeconds(SESSION_TTL_HOURS * 3600)
    }
}
