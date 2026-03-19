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

    override fun findOrCreate(tenantId: TenantId, phone: String): UUID =
        createCustomerUseCase.execute(
            CreateCustomerCommand(
                tenantId = tenantId,
                phone = phone,
                name = phone, // temporary name until customer provides it
                consentGiven = true // implicit LGPD consent by initiating contact
            )
        )
}
