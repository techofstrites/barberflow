package com.barberflow.scheduling.infrastructure.web

import com.barberflow.scheduling.infrastructure.persistence.ScheduleEntity
import com.barberflow.scheduling.infrastructure.persistence.ScheduleJpaRepository
import com.barberflow.scheduling.infrastructure.persistence.WorkingHoursEntity
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

data class WorkingHoursRequest(
    @field:NotNull val dayOfWeek: DayOfWeek,
    @field:NotNull val startTime: String, // "HH:mm"
    @field:NotNull val endTime: String    // "HH:mm"
)

data class UpsertScheduleRequest(
    val workingHours: List<WorkingHoursRequest> = emptyList()
)

data class WorkingHoursResponse(
    val dayOfWeek: DayOfWeek,
    val startTime: String,
    val endTime: String
)

data class ScheduleResponse(
    val professionalId: UUID,
    val workingHours: List<WorkingHoursResponse>
)

@RestController
@RequestMapping("/api/v1/professionals/{professionalId}/schedule")
class ScheduleController(private val jpa: ScheduleJpaRepository) {

    @GetMapping
    fun get(
        @PathVariable professionalId: UUID,
        @RequestHeader("X-Tenant-Id") tenantId: String
    ): ScheduleResponse {
        val tid = UUID.fromString(tenantId)
        val entity = jpa.findByTenantIdAndProfessionalId(tid, professionalId)
            ?: return ScheduleResponse(professionalId, emptyList())

        return entity.toResponse()
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    fun upsert(
        @PathVariable professionalId: UUID,
        @RequestHeader("X-Tenant-Id") tenantId: String,
        @Valid @RequestBody request: UpsertScheduleRequest
    ): ScheduleResponse {
        val tid = UUID.fromString(tenantId)
        val existing = jpa.findByTenantIdAndProfessionalId(tid, professionalId)

        val entity = existing ?: ScheduleEntity(
            id = UUID.randomUUID(),
            tenantId = tid,
            professionalId = professionalId
        )

        entity.workingHours.clear()
        request.workingHours.forEach { wh ->
            entity.workingHours.add(
                WorkingHoursEntity(
                    schedule = entity,
                    dayOfWeek = wh.dayOfWeek,
                    startTime = LocalTime.parse(wh.startTime),
                    endTime = LocalTime.parse(wh.endTime),
                    slotDurationMinutes = 30
                )
            )
        }

        return jpa.save(entity).toResponse()
    }

    private fun ScheduleEntity.toResponse() = ScheduleResponse(
        professionalId = professionalId,
        workingHours = workingHours.map {
            WorkingHoursResponse(
                dayOfWeek = it.dayOfWeek,
                startTime = it.startTime.toString(),
                endTime = it.endTime.toString()
            )
        }.sortedBy { it.dayOfWeek.value }
    )
}
