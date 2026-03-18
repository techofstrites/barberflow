package com.barberflow.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "jwt")
class JwtConfig {
    var secret: String = "barberflow-default-secret-change-in-production"
    var expirationMs: Long = 86_400_000 // 24 hours
    var issuer: String = "barberflow"
}
