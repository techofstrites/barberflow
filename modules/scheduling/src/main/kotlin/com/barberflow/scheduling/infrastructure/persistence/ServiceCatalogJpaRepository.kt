package com.barberflow.scheduling.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ServiceCatalogJpaRepository : JpaRepository<ServiceCatalogEntity, UUID> {
    fun findAllByTenantIdAndActiveTrue(tenantId: UUID): List<ServiceCatalogEntity>
    fun findByIdAndTenantId(id: UUID, tenantId: UUID): ServiceCatalogEntity?
}
