package com.barberflow.iam.infrastructure.web

import com.barberflow.iam.application.command.CreateTenantCommand
import com.barberflow.iam.application.command.CreateTenantResult
import com.barberflow.iam.application.command.CreateTenantUseCase
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

data class CreateTenantRequest(
    @field:NotBlank @field:Size(min = 3, max = 50) val slug: String,
    @field:NotBlank val name: String,
    @field:NotBlank val adminEmail: String,
    @field:NotBlank @field:Size(min = 8) val adminPassword: String
)

@RestController
@RequestMapping("/api/v1/tenants")
class TenantController(
    private val createTenantUseCase: CreateTenantUseCase
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateTenantRequest): CreateTenantResult =
        createTenantUseCase.execute(
            CreateTenantCommand(
                slug = request.slug,
                name = request.name,
                adminEmail = request.adminEmail,
                adminPassword = request.adminPassword
            )
        )
}
