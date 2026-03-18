package com.barberflow.iam.infrastructure.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.barberflow.iam.domain.model.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date

@Service
class JwtService(
    @Value("\${jwt.secret:barberflow-secret-change-in-production-min-32-chars}") private val secret: String,
    @Value("\${jwt.expiration-ms:900000}") private val expirationMs: Long,        // 15min access
    @Value("\${jwt.refresh-expiration-ms:604800000}") private val refreshMs: Long  // 7 days refresh
) {
    private val algorithm: Algorithm by lazy { Algorithm.HMAC256(secret) }

    fun generateAccessToken(user: User): String = JWT.create()
        .withIssuer("barberflow")
        .withSubject(user.id.toString())
        .withClaim("tenantId", user.tenantId.toString())
        .withClaim("email", user.email)
        .withClaim("role", user.role.name)
        .withIssuedAt(Date.from(Instant.now()))
        .withExpiresAt(Date.from(Instant.now().plusMillis(expirationMs)))
        .sign(algorithm)

    fun generateRefreshToken(user: User): String = JWT.create()
        .withIssuer("barberflow")
        .withSubject(user.id.toString())
        .withClaim("type", "refresh")
        .withIssuedAt(Date.from(Instant.now()))
        .withExpiresAt(Date.from(Instant.now().plusMillis(refreshMs)))
        .sign(algorithm)

    fun verifyAndGetSubject(token: String): String {
        val verifier = JWT.require(algorithm).withIssuer("barberflow").build()
        return verifier.verify(token).subject
    }

    fun getClaim(token: String, claim: String): String? = try {
        val verifier = JWT.require(algorithm).withIssuer("barberflow").build()
        verifier.verify(token).getClaim(claim).asString()
    } catch (e: JWTVerificationException) {
        null
    }
}
