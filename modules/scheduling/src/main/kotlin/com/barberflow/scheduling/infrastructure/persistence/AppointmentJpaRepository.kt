package com.barberflow.scheduling.infrastructure.persistence

import com.barberflow.scheduling.domain.model.AppointmentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface AppointmentJpaRepository : JpaRepository<AppointmentEntity, UUID> {
    fun findByIdAndTenantId(id: UUID, tenantId: UUID): AppointmentEntity?

    @Query("""
        SELECT a FROM AppointmentEntity a
        WHERE a.tenantId = :tenantId
          AND a.professionalId = :professionalId
          AND a.startAt < :endAt
          AND a.endAt > :startAt
    """)
    fun findOverlapping(
        @Param("tenantId") tenantId: UUID,
        @Param("professionalId") professionalId: UUID,
        @Param("startAt") startAt: Instant,
        @Param("endAt") endAt: Instant
    ): List<AppointmentEntity>

    fun findByStatusAndStartAtBefore(status: AppointmentStatus, startAt: Instant): List<AppointmentEntity>

    fun findByTenantIdAndCustomerId(tenantId: UUID, customerId: UUID): List<AppointmentEntity>

    @Query("""
        SELECT a FROM AppointmentEntity a
        WHERE a.tenantId = :tenantId
          AND a.startAt >= :from
          AND a.startAt < :to
    """)
    fun findByTenantIdAndDateRange(
        @Param("tenantId") tenantId: UUID,
        @Param("from") from: Instant,
        @Param("to") to: Instant
    ): List<AppointmentEntity>
}
