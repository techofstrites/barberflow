package com.barberflow.chatbot.infrastructure.scheduling

import com.barberflow.chatbot.domain.port.ProfessionalDto
import com.barberflow.chatbot.domain.port.ProfessionalQueryPort
import com.barberflow.core.tenant.TenantId
import com.barberflow.scheduling.infrastructure.web.ProfessionalJpaRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ProfessionalQueryAdapter(
    private val professionalJpa: ProfessionalJpaRepository
) : ProfessionalQueryPort {

    override fun findActiveProfessionals(tenantId: TenantId): List<ProfessionalDto> =
        professionalJpa.findAllByTenantIdAndActiveTrue(tenantId.value)
            .map { ProfessionalDto(it.id, it.name, it.specialties.toList()) }

    override fun findById(tenantId: TenantId, id: UUID): ProfessionalDto? =
        professionalJpa.findAllByTenantIdAndActiveTrue(tenantId.value)
            .find { it.id == id }
            ?.let { ProfessionalDto(it.id, it.name, it.specialties.toList()) }
}
