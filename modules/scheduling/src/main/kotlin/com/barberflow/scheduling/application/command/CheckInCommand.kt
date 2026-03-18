package com.barberflow.scheduling.application.command

import com.barberflow.core.tenant.TenantId
import java.util.UUID

data class CheckInCommand(
    val appointmentId: UUID,
    val tenantId: TenantId
)
