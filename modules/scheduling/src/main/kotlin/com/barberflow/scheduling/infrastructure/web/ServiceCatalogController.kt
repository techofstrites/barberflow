package com.barberflow.scheduling.infrastructure.web

import com.barberflow.core.tenant.TenantId
import com.barberflow.scheduling.domain.model.ServiceItem
import com.barberflow.scheduling.domain.repository.ServiceCatalogRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.util.UUID

data class CreateServiceRequest(
    @field:NotBlank val name: String,
    @field:Positive val price: Double,
    @field:Positive val durationMinutes: Int
)

data class UpdateServiceRequest(
    @field:NotBlank val name: String,
    @field:Positive val price: Double,
    @field:Positive val durationMinutes: Int
)

@RestController
@RequestMapping("/api/v1/services")
class ServiceCatalogController(
    private val serviceCatalogRepository: ServiceCatalogRepository
) {
    @GetMapping
    fun list(@RequestHeader("X-Tenant-Id") tenantId: String) =
        serviceCatalogRepository.findAll(TenantId.from(tenantId))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: CreateServiceRequest,
        @RequestHeader("X-Tenant-Id") tenantId: String
    ): ServiceItem {
        val service = ServiceItem(
            serviceId = UUID.randomUUID(),
            name = request.name,
            price = BigDecimal.valueOf(request.price),
            durationMinutes = request.durationMinutes
        )
        return serviceCatalogRepository.save(TenantId.from(tenantId), service)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateServiceRequest,
        @RequestHeader("X-Tenant-Id") tenantId: String
    ): ServiceItem {
        val tid = TenantId.from(tenantId)
        serviceCatalogRepository.findById(tid, id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found")
        val service = ServiceItem(
            serviceId = id,
            name = request.name,
            price = BigDecimal.valueOf(request.price),
            durationMinutes = request.durationMinutes
        )
        return serviceCatalogRepository.save(tid, service)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: UUID,
        @RequestHeader("X-Tenant-Id") tenantId: String
    ) {
        val tid = TenantId.from(tenantId)
        serviceCatalogRepository.findById(tid, id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found")
        serviceCatalogRepository.deactivate(tid, id)
    }
}
