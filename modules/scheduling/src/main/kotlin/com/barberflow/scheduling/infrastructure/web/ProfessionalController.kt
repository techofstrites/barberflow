package com.barberflow.scheduling.infrastructure.web

import jakarta.persistence.*
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "professionals")
class ProfessionalEntity(
    @Id val id: UUID = UUID.randomUUID(),
    @Column(name = "tenant_id", nullable = false) val tenantId: UUID,
    @Column(nullable = false) val name: String,
    @Column(columnDefinition = "TEXT[]") val specialties: Array<String> = emptyArray(),
    @Column(nullable = false) val active: Boolean = true,
    @Column(name = "created_at", nullable = false) val createdAt: Instant = Instant.now()
)

@Repository
interface ProfessionalJpaRepository : JpaRepository<ProfessionalEntity, UUID> {
    fun findAllByTenantIdAndActiveTrue(tenantId: UUID): List<ProfessionalEntity>
}

data class CreateProfessionalRequest(
    @field:NotBlank val name: String,
    val specialties: List<String> = emptyList()
)

data class ProfessionalResponse(
    val id: UUID,
    val name: String,
    val specialties: List<String>,
    val active: Boolean
)

@RestController
@RequestMapping("/api/v1/professionals")
class ProfessionalController(private val jpa: ProfessionalJpaRepository) {

    @GetMapping
    fun list(@RequestHeader("X-Tenant-Id") tenantId: String): List<ProfessionalResponse> =
        jpa.findAllByTenantIdAndActiveTrue(UUID.fromString(tenantId))
            .map { ProfessionalResponse(it.id, it.name, it.specialties.toList(), it.active) }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody req: CreateProfessionalRequest,
        @RequestHeader("X-Tenant-Id") tenantId: String
    ): ProfessionalResponse {
        val entity = jpa.save(
            ProfessionalEntity(
                tenantId = UUID.fromString(tenantId),
                name = req.name,
                specialties = req.specialties.toTypedArray()
            )
        )
        return ProfessionalResponse(entity.id, entity.name, entity.specialties.toList(), entity.active)
    }
}
