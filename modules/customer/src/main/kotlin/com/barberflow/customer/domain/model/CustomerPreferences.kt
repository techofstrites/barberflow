package com.barberflow.customer.domain.model

import java.util.UUID

enum class TimeOfDay { MORNING, AFTERNOON, EVENING }

data class CustomerPreferences(
    val preferredProfessionalId: UUID? = null,
    val preferredServiceIds: List<UUID> = emptyList(),
    val preferredTimeOfDay: TimeOfDay? = null
)
