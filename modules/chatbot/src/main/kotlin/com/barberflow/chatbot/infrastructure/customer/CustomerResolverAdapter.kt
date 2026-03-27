package com.barberflow.chatbot.infrastructure.customer

import com.barberflow.chatbot.domain.port.CustomerResolverPort
import com.barberflow.core.tenant.TenantId
import com.barberflow.customer.application.command.CreateCustomerCommand
import com.barberflow.customer.application.command.CreateCustomerUseCase
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CustomerResolverAdapter(
    private val createCustomerUseCase: CreateCustomerUseCase
) : CustomerResolverPort {

    override fun findOrCreate(tenantId: TenantId, phone: String, name: String?): UUID {
        val normalizedPhone = if (phone.startsWith("+")) phone else "+$phone"
        return createCustomerUseCase.execute(
            CreateCustomerCommand(
                tenantId = tenantId,
                phone = normalizedPhone,
                name = name?.takeIf { it.isNotBlank() } ?: normalizedPhone,
                consentGiven = true // implicit LGPD consent by initiating contact
            )
        )
    }
}
