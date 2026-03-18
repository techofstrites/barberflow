package com.barberflow.tenant

import com.barberflow.core.tenant.TenantId
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class TenantFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val tenantHeader = request.getHeader("X-Tenant-Id")
            if (!tenantHeader.isNullOrBlank()) {
                TenantContext.set(TenantId.from(tenantHeader))
            }
            filterChain.doFilter(request, response)
        } finally {
            TenantContext.clear()
        }
    }
}
