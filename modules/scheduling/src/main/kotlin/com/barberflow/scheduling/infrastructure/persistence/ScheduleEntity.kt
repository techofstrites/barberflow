package com.barberflow.scheduling.infrastructure.persistence

import com.barberflow.core.tenant.TenantId
import com.barberflow.scheduling.domain.model.Schedule
import com.barberflow.scheduling.domain.model.WorkingHours
import jakarta.persistence.*
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

@Entity
@Table(name = "schedules")
class ScheduleEntity(
    @Id
    val id: UUID,

    @Column(name = "tenant_id", nullable = false)
    val tenantId: UUID,

    @Column(name = "professional_id", nullable = false)
    val professionalId: UUID,

    @OneToMany(mappedBy = "schedule", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val workingHours: MutableList<WorkingHoursEntity> = mutableListOf()
) {
    fun toDomain() = Schedule(
        id = id,
        tenantId = TenantId(tenantId),
        professionalId = professionalId,
        workingHours = workingHours.map { it.toDomain() }
    )

    companion object {
        fun fromDomain(schedule: Schedule): ScheduleEntity {
            val entity = ScheduleEntity(
                id = schedule.id,
                tenantId = schedule.tenantId.value,
                professionalId = schedule.professionalId
            )
            schedule.workingHours.forEach { wh ->
                entity.workingHours.add(WorkingHoursEntity.fromDomain(entity, wh))
            }
            return entity
        }
    }
}

@Entity
@Table(name = "schedule_working_hours")
class WorkingHoursEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    val schedule: ScheduleEntity,

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    val dayOfWeek: DayOfWeek,

    @Column(name = "start_time", nullable = false)
    val startTime: LocalTime,

    @Column(name = "end_time", nullable = false)
    val endTime: LocalTime,

    @Column(name = "slot_duration_minutes", nullable = false)
    val slotDurationMinutes: Int = 30
) {
    fun toDomain() = WorkingHours(dayOfWeek, startTime, endTime, slotDurationMinutes)

    companion object {
        fun fromDomain(schedule: ScheduleEntity, wh: WorkingHours) = WorkingHoursEntity(
            schedule = schedule,
            dayOfWeek = wh.dayOfWeek,
            startTime = wh.startTime,
            endTime = wh.endTime,
            slotDurationMinutes = wh.slotDurationMinutes
        )
    }
}
