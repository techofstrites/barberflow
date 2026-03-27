package com.barberflow.chatbot.infrastructure.scheduling

import com.barberflow.chatbot.domain.port.AppointmentQueryPort
import com.barberflow.chatbot.domain.port.AppointmentSummaryDto
import com.barberflow.core.tenant.TenantId
import com.barberflow.customer.domain.repository.CustomerRepository
import com.barberflow.scheduling.domain.model.AppointmentStatus
import com.barberflow.scheduling.domain.repository.AppointmentRepository
import com.barberflow.scheduling.infrastructure.web.ProfessionalJpaRepository
import org.springframework.stereotype.Component
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

@Component
class AppointmentQueryAdapter(
    private val appointmentRepository: AppointmentRepository,
    private val customerRepository: CustomerRepository,
    private val professionalJpa: ProfessionalJpaRepository
) : AppointmentQueryPort {

    private val zone = ZoneId.systemDefault()

    override fun findUpcoming(tenantId: TenantId, customerPhone: String): List<AppointmentSummaryDto> {
        val normalizedPhone = if (customerPhone.startsWith("+")) customerPhone else "+$customerPhone"
        val customer = customerRepository.findByPhone(normalizedPhone, tenantId) ?: return emptyList()
        val now = ZonedDateTime.now(zone)
        return appointmentRepository.findByCustomerId(tenantId, customer.id)
            .filter { it.status in listOf(AppointmentStatus.PENDING_CONFIRMATION, AppointmentStatus.CONFIRMED) }
            .filter { it.timeSlot.startAt.isAfter(now) }
            .sortedBy { it.timeSlot.startAt }
            .take(5)
            .map { appt ->
                val profName = professionalJpa.findById(appt.professionalId).map { it.name }.orElse("Profissional")
                AppointmentSummaryDto(appt.id, profName, appt.timeSlot.startAt, appt.status.name)
            }
    }

    override fun findRecent(tenantId: TenantId, customerPhone: String): List<AppointmentSummaryDto> {
        val normalizedPhone = if (customerPhone.startsWith("+")) customerPhone else "+$customerPhone"
        val customer = customerRepository.findByPhone(normalizedPhone, tenantId) ?: return emptyList()
        val now = ZonedDateTime.now(zone)
        val pastMonth = now.minusDays(90)
        return appointmentRepository.findByCustomerId(tenantId, customer.id)
            .filter { it.timeSlot.startAt.isAfter(pastMonth) }
            .sortedByDescending { it.timeSlot.startAt }
            .take(5)
            .map { appt ->
                val profName = professionalJpa.findById(appt.professionalId).map { it.name }.orElse("Profissional")
                AppointmentSummaryDto(appt.id, profName, appt.timeSlot.startAt, appt.status.name)
            }
    }
}
