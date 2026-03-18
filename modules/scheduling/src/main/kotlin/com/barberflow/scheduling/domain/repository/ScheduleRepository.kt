package com.barberflow.scheduling.domain.repository

import com.barberflow.core.tenant.TenantId
import com.barberflow.scheduling.domain.model.Schedule
import java.util.UUID

interface ScheduleRepository {
    fun save(schedule: Schedule): Schedule
    fun findByProfessionalId(tenantId: TenantId, professionalId: UUID): Schedule?
    fun findAllByTenantId(tenantId: TenantId): List<Schedule>
}
