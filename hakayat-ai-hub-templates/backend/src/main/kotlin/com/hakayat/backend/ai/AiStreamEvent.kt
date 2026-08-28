package com.hakayat.backend.ai

sealed interface AiStreamEvent {
    data class Started(val responseId: String? = null) : AiStreamEvent
    data class TextDelta(val text: String) : AiStreamEvent
    data class Completed(val response: StreamResponse) : AiStreamEvent
}

data class StreamResponse(
    val id: String? = null,
    val status: String? = null,
    val usage: TokenUsage = TokenUsage(),
)

data class TokenUsage(val inputTokens: Long = 0, val outputTokens: Long = 0)
