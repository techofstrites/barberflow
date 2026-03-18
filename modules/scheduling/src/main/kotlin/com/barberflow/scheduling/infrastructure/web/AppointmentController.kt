package com.barberflow.scheduling.infrastructure.web

import com.barberflow.core.tenant.TenantId
import com.barberflow.scheduling.application.command.*
import com.barberflow.scheduling.domain.repository.AppointmentRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.ZonedDateTime
import java.util.UUID

data class ScheduleAppointmentRequest(
    @field:NotNull val customerId: UUID,
    @field:NotNull val professionalId: UUID,
    @field:NotNull val serviceIds: List<UUID>,
    @field:NotNull val startAt: ZonedDateTime
)

data class CancelAppointmentRequest(val reason: String?)

data class ServiceItemResponse(val serviceId: UUID, val name: String, val price: Double, val durationMinutes: Int)
data class AppointmentResponse(
    val id: UUID, val customerId: UUID, val professionalId: UUID,
    val services: List<ServiceItemResponse>,
    val startAt: String, val endAt: String,
    val status: String, val paymentStatus: String,
    val suggestedByAI: Boolean
)

@RestController
@RequestMapping("/api/v1/appointments")
class AppointmentController(
    private val scheduleUseCase: ScheduleAppointmentUseCase,
    private val cancelUseCase: CancelAppointmentUseCase,
    private val completeUseCase: CompleteAppointmentUseCase,
    private val appointmentRepository: AppointmentRepository
) {
    @GetMapping
    fun list(
        @RequestHeader("X-Tenant-Id") tenantId: String,
        @RequestParam from: ZonedDateTime,
        @RequestParam to: ZonedDateTime
    ): List<AppointmentResponse> =
        appointmentRepository.findByTenantAndDateRange(TenantId.from(tenantId), from, to)
            .map { appt ->
                AppointmentResponse(
                    id = appt.id,
                    customerId = appt.customerId,
                    professionalId = appt.professionalId,
                    services = appt.services.map { s ->
                        ServiceItemResponse(s.serviceId, s.name, s.price.toDouble(), s.durationMinutes)
                    },
                    startAt = appt.timeSlot.startAt.toString(),
                    endAt = appt.timeSlot.endAt.toString(),
                    status = appt.status.name,
                    paymentStatus = appt.paymentStatus.name,
                    suggestedByAI = appt.suggestedByAI
                )
            }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun schedule(
        @Valid @RequestBody request: ScheduleAppointmentRequest,
        @RequestHeader("X-Tenant-Id") tenantId: String
    ): Map<String, UUID> {
        val id = scheduleUseCase.execute(
            ScheduleAppointmentCommand(
                tenantId = TenantId.from(tenantId),
                customerId = request.customerId,
                professionalId = request.professionalId,
                serviceIds = request.serviceIds,
                startAt = request.startAt
            )
        )
        return mapOf("appointmentId" to id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancel(
        @PathVariable id: UUID,
        @RequestBody(required = false) request: CancelAppointmentRequest?,
        @RequestHeader("X-Tenant-Id") tenantId: String
    ) {
        cancelUseCase.execute(
            CancelAppointmentCommand(
                appointmentId = id,
                tenantId = TenantId.from(tenantId),
                reason = request?.reason
            )
        )
    }

    @PostMapping("/{id}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun complete(
        @PathVariable id: UUID,
        @RequestHeader("X-Tenant-Id") tenantId: String
    ) {
        completeUseCase.execute(CompleteAppointmentCommand(id, TenantId.from(tenantId)))
    }
}
