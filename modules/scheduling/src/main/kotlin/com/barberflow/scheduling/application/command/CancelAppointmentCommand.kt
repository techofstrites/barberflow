package com.barberflow.scheduling.application.command

import com.barberflow.core.tenant.TenantId
import java.util.UUID

data class CancelAppointmentCommand(
    val appointmentId: UUID,
    val tenantId: TenantId,
    val reason: String? = null
)
