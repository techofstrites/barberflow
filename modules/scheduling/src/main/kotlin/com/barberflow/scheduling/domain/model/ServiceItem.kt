package com.barberflow.scheduling.domain.model

import java.math.BigDecimal
import java.util.UUID

data class ServiceItem(
    val serviceId: UUID,
    val name: String,
    val price: BigDecimal,
    val durationMinutes: Int
)
