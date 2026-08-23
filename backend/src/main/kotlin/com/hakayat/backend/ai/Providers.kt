package com.hakayat.backend.ai

import kotlinx.serialization.Serializable

@Serializable
data class LlmRequest(val system: String, val prompt: String, val temperature: Double = 0.7)

interface LlmProvider {
    val name: String
    suspend fun complete(request: LlmRequest): String
}

class EnvBackedProviderRegistry(private val providers: List<LlmProvider>) {
    fun default(): LlmProvider = providers.firstOrNull() ?: error("No AI provider configured")
}

class DisabledLlmProvider : LlmProvider {
    override val name: String = "disabled"
    override suspend fun complete(request: LlmRequest): String = error("AI provider is not configured")
}
