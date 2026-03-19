package com.barberflow.chatbot.domain.model

enum class ConversationState {
    IDLE,
    GREETING,
    SERVICE_SELECTION,
    PROFESSIONAL_SELECTION,
    DAY_SELECTION,
    SLOT_SELECTION,
    AWAITING_CONFIRMATION,
    AWAITING_PAYMENT,
    COMPLETED,
    TRANSFERRED_TO_HUMAN
}
