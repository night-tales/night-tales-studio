package com.hakayat.backend.ai

enum class AiProvider {
    OPENAI,
    ANTHROPIC,
    GEMINI
}

data class AiRequest(
    val model: String,
    val prompt: String,
    val temperature: Double? = null,
    val maxTokens: Int? = null
)

data class AiUsage(
    val inputTokens: Long = 0,
    val outputTokens: Long = 0,
    val totalTokens: Long = inputTokens + outputTokens
)

data class AiResponse(
    val text: String,
    val usage: AiUsage = AiUsage(),
    val finishReason: String? = null,
    val providerRequestId: String? = null
)

interface AiProviderAdapter {
    val provider: AiProvider
    suspend fun complete(request: AiRequest): Result<AiResponse>
}
