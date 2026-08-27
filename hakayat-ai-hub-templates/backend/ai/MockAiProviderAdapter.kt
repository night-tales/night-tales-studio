package com.hakayat.backend.ai

class MockAiProviderAdapter : AiProviderAdapter {
    override val provider: AiProvider = AiProvider.OPENAI

    override suspend fun complete(request: AiRequest): Result<AiResponse> {
        if (request.prompt.isBlank()) {
            return Result.failure(IllegalArgumentException("Prompt must not be blank"))
        }

        return Result.success(
            AiResponse(
                text = "Mock response for model " + request.model,
                usage = AiUsage(inputTokens = request.prompt.length.toLong(), outputTokens = 5),
                finishReason = "stop",
                providerRequestId = "mock-" + request.model
            )
        )
    }
}
