package com.barberflow.scheduling.domain.model

import com.barberflow.core.tenant.TenantId
import java.util.UUID

data class Professional(
    val id: UUID,
    val tenantId: TenantId,
    val name: String,
    val specialties: List<String> = emptyList(),
    val active: Boolean = true
)
