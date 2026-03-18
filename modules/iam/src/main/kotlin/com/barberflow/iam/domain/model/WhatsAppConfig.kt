package com.barberflow.iam.domain.model

data class WhatsAppConfig(
    val phoneNumberId: String,
    val accessToken: String  // stored encrypted
)
