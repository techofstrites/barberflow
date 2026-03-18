package com.barberflow.scheduling.infrastructure.persistence

import com.barberflow.core.tenant.TenantId
import com.barberflow.scheduling.domain.model.ServiceItem
import com.barberflow.scheduling.domain.repository.ServiceCatalogRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ServiceCatalogRepositoryImpl(
    private val jpa: ServiceCatalogJpaRepository
) : ServiceCatalogRepository {

    override fun findById(tenantId: TenantId, serviceId: UUID): ServiceItem? =
        jpa.findByIdAndTenantId(serviceId, tenantId.value)?.toDomain()

    override fun findAll(tenantId: TenantId): List<ServiceItem> =
        jpa.findAllByTenantIdAndActiveTrue(tenantId.value).map { it.toDomain() }

    override fun save(tenantId: TenantId, service: ServiceItem): ServiceItem {
        jpa.save(
            ServiceCatalogEntity(
                id = service.serviceId,
                tenantId = tenantId.value,
                name = service.name,
                price = service.price,
                durationMinutes = service.durationMinutes
            )
        )
        return service
    }
}
