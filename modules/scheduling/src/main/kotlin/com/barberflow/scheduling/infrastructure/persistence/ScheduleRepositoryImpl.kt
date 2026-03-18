package com.barberflow.scheduling.infrastructure.persistence

import com.barberflow.core.tenant.TenantId
import com.barberflow.scheduling.domain.model.Schedule
import com.barberflow.scheduling.domain.repository.ScheduleRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ScheduleRepositoryImpl(
    private val jpa: ScheduleJpaRepository
) : ScheduleRepository {

    override fun save(schedule: Schedule): Schedule {
        jpa.save(ScheduleEntity.fromDomain(schedule))
        return schedule
    }

    override fun findByProfessionalId(tenantId: TenantId, professionalId: UUID): Schedule? =
        jpa.findByTenantIdAndProfessionalId(tenantId.value, professionalId)?.toDomain()

    override fun findAllByTenantId(tenantId: TenantId): List<Schedule> =
        jpa.findAllByTenantId(tenantId.value).map { it.toDomain() }
}
