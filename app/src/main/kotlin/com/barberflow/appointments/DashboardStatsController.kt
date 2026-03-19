package com.barberflow.appointments

import com.barberflow.core.tenant.TenantId
import com.barberflow.customer.infrastructure.persistence.CustomerJpaRepository
import com.barberflow.scheduling.domain.model.AppointmentStatus
import com.barberflow.scheduling.domain.repository.AppointmentRepository
import org.springframework.web.bind.annotation.*
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

data class DashboardStatsResponse(
    val appointmentsToday: Int,
    val revenueThisMonth: Double,
    val activeCustomers: Int,
    val returnRate: Int
)

@RestController
@RequestMapping("/api/v1/dashboard/stats")
class DashboardStatsController(
    private val appointmentRepository: AppointmentRepository,
    private val customerJpa: CustomerJpaRepository
) {
    private val zone = ZoneId.of("America/Sao_Paulo")

    @GetMapping
    fun stats(@RequestHeader("X-Tenant-Id") tenantId: String): DashboardStatsResponse {
        val tid = TenantId.from(tenantId)
        val now = ZonedDateTime.now(zone)
        val tenantUuid = UUID.fromString(tenantId)

        val todayStart = now.toLocalDate().atStartOfDay(zone)
        val todayEnd = todayStart.plusDays(1)
        val appointmentsToday = appointmentRepository
            .findByTenantAndDateRange(tid, todayStart, todayEnd).size

        val monthStart = now.toLocalDate().withDayOfMonth(1).atStartOfDay(zone)
        val monthEnd = monthStart.plusMonths(1)
        val revenueThisMonth = appointmentRepository
            .findByTenantAndDateRange(tid, monthStart, monthEnd)
            .filter { it.status in listOf(AppointmentStatus.COMPLETED, AppointmentStatus.IN_PROGRESS) }
            .flatMap { it.services }
            .sumOf { it.price }
            .toDouble()

        val activeCustomers = customerJpa.findAllByTenantId(tenantUuid).size

        val yearAppts = appointmentRepository
            .findByTenantAndDateRange(tid, now.minusYears(1), now)
            .filter { it.status != AppointmentStatus.CANCELLED }
        val customerGroups = yearAppts.groupBy { it.customerId }
        val returnRate = if (customerGroups.isEmpty()) 0
        else customerGroups.count { (_, appts) -> appts.size > 1 } * 100 / customerGroups.size

        return DashboardStatsResponse(
            appointmentsToday = appointmentsToday,
            revenueThisMonth = revenueThisMonth,
            activeCustomers = activeCustomers,
            returnRate = returnRate
        )
    }
}
