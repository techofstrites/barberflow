package com.barberflow.iam.application.query

import com.barberflow.iam.domain.model.User
import com.barberflow.iam.domain.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

data class GetUserQuery(val userId: UUID)

@Service
class GetUserQueryHandler(
    private val userRepository: UserRepository
) {
    fun handle(query: GetUserQuery): User? =
        userRepository.findById(query.userId)
}
