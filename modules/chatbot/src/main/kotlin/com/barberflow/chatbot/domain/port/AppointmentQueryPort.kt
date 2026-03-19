package com.barberflow.chatbot.domain.port

import com.barberflow.core.tenant.TenantId
import java.time.ZonedDateTime
import java.util.UUID

data class AppointmentSummaryDto(
    val id: UUID,
    val professionalName: String,
    val startAt: ZonedDateTime,
    val status: String
)

interface AppointmentQueryPort {
    fun findUpcoming(tenantId: TenantId, customerPhone: String): List<AppointmentSummaryDto>
    fun findRecent(tenantId: TenantId, customerPhone: String): List<AppointmentSummaryDto>
}
