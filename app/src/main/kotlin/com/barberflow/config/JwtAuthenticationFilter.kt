package com.barberflow.config

import com.barberflow.iam.infrastructure.security.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val token = authHeader.removePrefix("Bearer ").trim()
            val userId = jwtService.verifyAndGetSubject(token)
            val role = jwtService.getClaim(token, "role") ?: "USER"

            val auth = UsernamePasswordAuthenticationToken(
                userId,
                null,
                listOf(SimpleGrantedAuthority("ROLE_$role"))
            )
            SecurityContextHolder.getContext().authentication = auth
        } catch (e: Exception) {
            logger.debug("JWT validation failed: ${e.message}")
        }

        filterChain.doFilter(request, response)
    }
}
