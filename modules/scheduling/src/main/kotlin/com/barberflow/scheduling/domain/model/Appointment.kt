package com.barberflow.scheduling.domain.model

import com.barberflow.core.domain.AggregateRoot
import com.barberflow.core.tenant.TenantId
import com.barberflow.scheduling.domain.event.*
import java.time.Instant
import java.util.UUID

enum class PaymentStatus { UNPAID, PARTIALLY_PAID, PAID }

class Appointment private constructor(
    val id: UUID,
    val tenantId: TenantId,
    val customerId: UUID,
    val professionalId: UUID,
    val services: List<ServiceItem>,
    val timeSlot: TimeSlot,
    var status: AppointmentStatus,
    var paymentStatus: PaymentStatus,
    val createdAt: Instant,
    val suggestedByAI: Boolean
) : AggregateRoot() {

    companion object {
        fun schedule(
            tenantId: TenantId,
            customerId: UUID,
            professionalId: UUID,
            services: List<ServiceItem>,
            timeSlot: TimeSlot,
            suggestedByAI: Boolean = false
        ): Appointment {
            require(services.isNotEmpty()) { "Appointment must have at least one service" }

            val appointment = Appointment(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                customerId = customerId,
                professionalId = professionalId,
                services = services,
                timeSlot = timeSlot,
                status = AppointmentStatus.PENDING_CONFIRMATION,
                paymentStatus = PaymentStatus.UNPAID,
                createdAt = Instant.now(),
                suggestedByAI = suggestedByAI
            )
            appointment.registerEvent(
                AppointmentScheduled(appointment.id, tenantId, customerId, professionalId, timeSlot)
            )
            return appointment
        }

        fun reconstitute(
            id: UUID, tenantId: TenantId, customerId: UUID, professionalId: UUID,
            services: List<ServiceItem>, timeSlot: TimeSlot, status: AppointmentStatus,
            paymentStatus: PaymentStatus, createdAt: Instant, suggestedByAI: Boolean
        ) = Appointment(id, tenantId, customerId, professionalId, services, timeSlot, status, paymentStatus, createdAt, suggestedByAI)
    }

    fun confirm() {
        require(status == AppointmentStatus.PENDING_CONFIRMATION) {
            "Only PENDING_CONFIRMATION appointments can be confirmed"
        }
        status = AppointmentStatus.CONFIRMED
        registerEvent(AppointmentConfirmed(id, tenantId, customerId))
    }

    fun complete() {
        require(status in listOf(AppointmentStatus.CONFIRMED, AppointmentStatus.IN_PROGRESS)) {
            "Only CONFIRMED or IN_PROGRESS appointments can be completed"
        }
        status = AppointmentStatus.COMPLETED
        registerEvent(AppointmentCompleted(id, tenantId, customerId, professionalId, services))
    }

    fun cancel(reason: String? = null) {
        require(status !in listOf(AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED)) {
            "Cannot cancel a COMPLETED or already CANCELLED appointment"
        }
        status = AppointmentStatus.CANCELLED
        registerEvent(AppointmentCancelled(id, tenantId, customerId, reason))
    }

    fun markNoShow() {
        require(status == AppointmentStatus.CONFIRMED) {
            "Only CONFIRMED appointments can be marked as NO_SHOW"
        }
        status = AppointmentStatus.NO_SHOW
        registerEvent(AppointmentNoShow(id, tenantId, customerId))
    }

    fun registerPayment(partial: Boolean = false) {
        paymentStatus = if (partial) PaymentStatus.PARTIALLY_PAID else PaymentStatus.PAID
        if (paymentStatus == PaymentStatus.PAID && status == AppointmentStatus.PENDING_CONFIRMATION) {
            confirm()
        }
    }
}
