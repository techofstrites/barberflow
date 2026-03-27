package com.barberflow.chatbot.domain.port

import com.barberflow.core.tenant.TenantId
import java.math.BigDecimal
import java.util.UUID

data class ServiceCatalogItemDto(
    val id: UUID,
    val name: String,
    val price: BigDecimal,
    val durationMinutes: Int
)

interface ServiceQueryPort {
    fun findAll(tenantId: TenantId): List<ServiceCatalogItemDto>
}
