package com.barberflow.chatbot.domain.model

import java.time.Instant

data class ConversationMessage(
    val direction: MessageDirection,
    val content: String,
    val timestamp: Instant = Instant.now()
)
