package com.barberflow.customer.application.command

import com.barberflow.customer.domain.model.Customer
import com.barberflow.customer.domain.repository.CustomerRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CreateCustomerUseCase(
    private val customerRepository: CustomerRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    @Transactional
    fun execute(command: CreateCustomerCommand): UUID {
        if (customerRepository.existsByPhone(command.phone, command.tenantId)) {
            return customerRepository.findByPhone(command.phone, command.tenantId)!!.id
        }

        val customer = Customer.create(
            tenantId = command.tenantId,
            phone = command.phone,
            name = command.name,
            consentGiven = command.consentGiven
        )
        customerRepository.save(customer)
        customer.domainEvents.forEach { eventPublisher.publishEvent(it) }
        customer.clearEvents()

        return customer.id
    }
}
