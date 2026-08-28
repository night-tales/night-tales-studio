package com.hakayat.backend.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {
    private val secret: String by lazy {
        System.getenv("JWT_SECRET")?.takeIf { it.isNotBlank() }
            ?: error("JWT_SECRET must be configured; refusing to start with a default secret")
    }

    private const val issuer = "hakayat-ai-hub"
    private const val audience = "hakayat-mobile-app"
    private const val tokenLifetimeMs = 24 * 60 * 60 * 1000L

    val verifier = JWT
        .require(Algorithm.HMAC256(secret))
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun generateToken(userId: String): String {
        require(userId.isNotBlank()) { "userId must not be blank" }

        val now = Date()
        val expiresAt = Date(now.time + tokenLifetimeMs)

        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withSubject(userId)
            .withClaim("userId", userId)
            .withIssuedAt(now)
            .withExpiresAt(expiresAt)
            .sign(Algorithm.HMAC256(secret))
    }
}
