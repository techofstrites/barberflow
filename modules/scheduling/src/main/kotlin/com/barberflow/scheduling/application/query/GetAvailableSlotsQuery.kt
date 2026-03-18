package com.barberflow.scheduling.application.query

import com.barberflow.core.tenant.TenantId
import java.time.LocalDate
import java.util.UUID

data class GetAvailableSlotsQuery(
    val tenantId: TenantId,
    val professionalId: UUID,
    val date: LocalDate,
    val serviceDurationMinutes: Int
)
