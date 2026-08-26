package com.hakayat.backend.llamagen

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header
import io.ktor.http.HttpHeaders
import kotlinx.serialization.Serializable
import java.security.MessageDigest

@Serializable
data class LlamaGenWebhookEvent(
    val event: String,
    val generationId: String,
    val status: LlamaGenStatus,
    val assetUrl: String? = null,
    val createdAt: String? = null
)

fun ApplicationCall.hasValidLlamaGenWebhookSecret(expected: String): Boolean {
    val supplied = request.header(HttpHeaders.Authorization)
        ?.removePrefix("Bearer ")
        ?.trim()
        ?: request.header("X-LlamaGen-Webhook-Secret")
        ?: return false
    if (expected.isEmpty() || supplied.isEmpty()) return false
    return MessageDigest.isEqual(supplied.toByteArray(), expected.toByteArray())
}
