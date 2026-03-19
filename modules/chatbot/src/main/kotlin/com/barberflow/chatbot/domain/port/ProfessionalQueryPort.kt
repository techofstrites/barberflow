package com.barberflow.chatbot.domain.port

import com.barberflow.core.tenant.TenantId
import java.util.UUID

data class ProfessionalDto(
    val id: UUID,
    val name: String,
    val specialties: List<String>
)

interface ProfessionalQueryPort {
    fun findActiveProfessionals(tenantId: TenantId): List<ProfessionalDto>
    fun findById(tenantId: TenantId, id: UUID): ProfessionalDto?
}
