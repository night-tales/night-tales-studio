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

sealed interface AiStreamEvent {
    data class Started(val providerRequestId: String? = null) : AiStreamEvent
    data class TextDelta(val text: String) : AiStreamEvent
    data class Completed(val response: AiResponse) : AiStreamEvent
    data class Failed(val error: Throwable) : AiStreamEvent
}

interface AiProviderAdapter {
    val provider: AiProvider
    suspend fun complete(request: AiRequest): Result<AiResponse>
    fun stream(request: AiRequest): kotlinx.coroutines.flow.Flow<AiStreamEvent> =
        kotlinx.coroutines.flow.flow {
            complete(request)
                .onSuccess { emit(AiStreamEvent.Completed(it)) }
                .onFailure { emit(AiStreamEvent.Failed(it)) }
        }
}
