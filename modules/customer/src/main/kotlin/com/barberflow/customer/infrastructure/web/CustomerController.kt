package com.barberflow.customer.infrastructure.web

import com.barberflow.core.tenant.TenantId
import com.barberflow.customer.application.command.CreateCustomerCommand
import com.barberflow.customer.application.command.CreateCustomerUseCase
import com.barberflow.customer.application.query.GetCustomerByPhoneQuery
import com.barberflow.customer.application.query.GetCustomerByPhoneQueryHandler
import com.barberflow.customer.domain.repository.CustomerRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

data class CreateCustomerRequest(
    @field:NotBlank
    @field:Pattern(regexp = "\\+[1-9]\\d{7,14}", message = "Phone must be in E.164 format")
    val phone: String,
    @field:NotBlank val name: String,
    val consentGiven: Boolean = false
)

data class CustomerResponse(
    val id: UUID,
    val phone: String,
    val name: String,
    val consentGiven: Boolean
)

@RestController
@RequestMapping("/api/v1/customers")
class CustomerController(
    private val createCustomerUseCase: CreateCustomerUseCase,
    private val getCustomerByPhoneQueryHandler: GetCustomerByPhoneQueryHandler,
    private val customerRepository: CustomerRepository
) {
    @GetMapping
    fun list(@RequestHeader("X-Tenant-Id") tenantId: String): List<CustomerResponse> =
        customerRepository.findAll(TenantId.from(tenantId)).map { c ->
            CustomerResponse(id = c.id, phone = c.phone, name = c.name, consentGiven = c.consentGiven)
        }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: CreateCustomerRequest,
        @RequestHeader("X-Tenant-Id") tenantId: String
    ): Map<String, UUID> {
        val id = createCustomerUseCase.execute(
            CreateCustomerCommand(
                tenantId = TenantId.from(tenantId),
                phone = request.phone,
                name = request.name,
                consentGiven = request.consentGiven
            )
        )
        return mapOf("customerId" to id)
    }

    @GetMapping("/by-phone")
    fun findByPhone(
        @RequestParam phone: String,
        @RequestHeader("X-Tenant-Id") tenantId: String
    ): CustomerResponse {
        val customer = getCustomerByPhoneQueryHandler.handle(
            GetCustomerByPhoneQuery(phone, TenantId.from(tenantId))
        ) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found")

        return CustomerResponse(
            id = customer.id,
            phone = customer.phone,
            name = customer.name,
            consentGiven = customer.consentGiven
        )
    }
}
