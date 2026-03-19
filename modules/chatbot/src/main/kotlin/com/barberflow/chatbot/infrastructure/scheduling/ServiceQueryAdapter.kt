package com.barberflow.chatbot.infrastructure.scheduling

import com.barberflow.chatbot.domain.port.ServiceCatalogItemDto
import com.barberflow.chatbot.domain.port.ServiceQueryPort
import com.barberflow.core.tenant.TenantId
import com.barberflow.scheduling.domain.repository.ServiceCatalogRepository
import org.springframework.stereotype.Component

@Component
class ServiceQueryAdapter(
    private val serviceCatalogRepository: ServiceCatalogRepository
) : ServiceQueryPort {

    override fun findAll(tenantId: TenantId): List<ServiceCatalogItemDto> =
        serviceCatalogRepository.findAll(tenantId).map { s ->
            ServiceCatalogItemDto(
                id = s.serviceId,
                name = s.name,
                price = s.price,
                durationMinutes = s.durationMinutes
            )
        }
}
