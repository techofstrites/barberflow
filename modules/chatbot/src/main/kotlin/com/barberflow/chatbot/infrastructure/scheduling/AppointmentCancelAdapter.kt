package com.barberflow.chatbot.infrastructure.scheduling

import com.barberflow.chatbot.domain.port.AppointmentCancelPort
import com.barberflow.core.tenant.TenantId
import com.barberflow.scheduling.application.command.CancelAppointmentCommand
import com.barberflow.scheduling.application.command.CancelAppointmentUseCase
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AppointmentCancelAdapter(
    private val cancelUseCase: CancelAppointmentUseCase
) : AppointmentCancelPort {

    override fun cancel(tenantId: TenantId, appointmentId: UUID) {
        cancelUseCase.execute(
            CancelAppointmentCommand(
                appointmentId = appointmentId,
                tenantId = tenantId,
                reason = "Cancelado pelo cliente via WhatsApp"
            )
        )
    }
}
