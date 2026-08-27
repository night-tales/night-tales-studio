package com.hakayat.backend.auth

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

object JwtConfig {
    private val secret = System.getenv("JWT_SECRET") ?: "super-secret-local-key-12345"
    private val issuer = "hakayat-ai-hub"
    private val audience = "hakayat-mobile-app"

    val verifier = JWT
        .require(Algorithm.HMAC256(secret))
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun generateToken(userId: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .sign(Algorithm.HMAC256(secret))
    }
}
