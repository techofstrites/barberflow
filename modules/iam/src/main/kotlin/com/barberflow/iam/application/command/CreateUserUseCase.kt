package com.barberflow.iam.application.command

import com.barberflow.iam.domain.model.User
import com.barberflow.iam.domain.repository.UserRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CreateUserUseCase(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val eventPublisher: ApplicationEventPublisher
) {
    @Transactional
    fun execute(command: CreateUserCommand): UUID {
        require(userRepository.findByEmail(command.email) == null) {
            "Email '${command.email}' already registered"
        }

        val user = User.create(
            tenantId = command.tenantId,
            email = command.email,
            passwordHash = passwordEncoder.encode(command.password),
            role = command.role
        )
        userRepository.save(user)
        user.domainEvents.forEach { eventPublisher.publishEvent(it) }
        user.clearEvents()

        return user.id
    }
}
