package com.barberflow.appointments

import com.barberflow.core.tenant.TenantId
import com.barberflow.customer.infrastructure.persistence.CustomerJpaRepository
import com.barberflow.scheduling.domain.repository.AppointmentRepository
import com.barberflow.scheduling.infrastructure.web.ProfessionalJpaRepository
import org.springframework.web.bind.annotation.*
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class AppointmentDetailResponse(
    val id: UUID,
    val customerName: String,
    val customerPhone: String,
    val professionalName: String,
    val startAt: String,
    val endAt: String,
    val status: String,
    val services: List<String>
)

@RestController
@RequestMapping("/api/v1/appointments/details")
class AppointmentDetailsController(
    private val appointmentRepository: AppointmentRepository,
    private val customerJpa: CustomerJpaRepository,
    private val professionalJpa: ProfessionalJpaRepository
) {
    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    private val zone = ZoneId.of("America/Sao_Paulo")

    @GetMapping
    fun list(
        @RequestHeader("X-Tenant-Id") tenantId: String,
        @RequestParam(defaultValue = "") from: String,
        @RequestParam(defaultValue = "") to: String
    ): List<AppointmentDetailResponse> {
        val tid = TenantId.from(tenantId)
        val tenantUuid = UUID.fromString(tenantId)

        val fromDt = if (from.isNotBlank()) ZonedDateTime.parse(from) else ZonedDateTime.now(zone).minusMonths(1)
        val toDt = if (to.isNotBlank()) ZonedDateTime.parse(to) else ZonedDateTime.now(zone).plusMonths(1)

        val appointments = appointmentRepository.findByTenantAndDateRange(tid, fromDt, toDt)

        val customerIds = appointments.map { it.customerId }.toSet()
        val professionalIds = appointments.map { it.professionalId }.toSet()

        val customerMap = customerJpa.findAllByTenantId(tenantUuid)
            .filter { it.id in customerIds }
            .associateBy { it.id }

        val professionalMap = professionalJpa.findAllByTenantId(tenantUuid)
            .filter { it.id in professionalIds }
            .associateBy { it.id }

        return appointments
            .sortedBy { it.timeSlot.startAt }
            .map { appt ->
                val customer = customerMap[appt.customerId]
                val professional = professionalMap[appt.professionalId]
                AppointmentDetailResponse(
                    id = appt.id,
                    customerName = customer?.name ?: "Desconhecido",
                    customerPhone = customer?.phone ?: "-",
                    professionalName = professional?.name ?: "Desconhecido",
                    startAt = appt.timeSlot.startAt.withZoneSameInstant(zone).format(formatter),
                    endAt = appt.timeSlot.endAt.withZoneSameInstant(zone).format(formatter),
                    status = appt.status.name,
                    services = appt.services.map { it.name }
                )
            }
    }
}
