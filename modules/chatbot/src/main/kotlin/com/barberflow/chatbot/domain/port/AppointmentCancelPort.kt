package com.barberflow.chatbot.domain.port

import com.barberflow.core.tenant.TenantId
import java.util.UUID

interface AppointmentCancelPort {
    fun cancel(tenantId: TenantId, appointmentId: UUID)
}
