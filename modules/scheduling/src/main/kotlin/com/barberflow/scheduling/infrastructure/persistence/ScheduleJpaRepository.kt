package com.barberflow.scheduling.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ScheduleJpaRepository : JpaRepository<ScheduleEntity, UUID> {
    fun findByTenantIdAndProfessionalId(tenantId: UUID, professionalId: UUID): ScheduleEntity?
    fun findAllByTenantId(tenantId: UUID): List<ScheduleEntity>
}
