package com.barberflow.scheduling.application.command

import com.barberflow.core.tenant.TenantId
import java.time.ZonedDateTime
import java.util.UUID

data class ScheduleAppointmentCommand(
    val tenantId: TenantId,
    val customerId: UUID,
    val professionalId: UUID,
    val serviceIds: List<UUID>,
    val startAt: ZonedDateTime,
    val suggestedByAI: Boolean = false
)
