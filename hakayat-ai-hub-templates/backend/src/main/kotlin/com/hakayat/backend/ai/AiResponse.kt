package com.hakayat.backend.ai

data class AiResponse(
    val text: String,
    val finishReason: String? = null,
    val usage: TokenUsage = TokenUsage(),
    val providerRequestId: String? = null,
)
