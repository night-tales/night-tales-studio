package com.hakayat.backend.ai

class MockAiProviderAdapter : AiAgentAdapter {
    override val provider: AiProvider = AiProvider.OPENAI

    override suspend fun execute(request: AiRequest): AiResponse = AiResponse(
        text = "Mock response for model " + request.model,
        finishReason = "stop",
        usage = TokenUsage(inputTokens = 3, outputTokens = 5),
        providerRequestId = "mock-request",
    )

    override fun stream(request: AiRequest) = kotlinx.coroutines.flow.flow {
        emit(AiStreamEvent.Completed(StreamResponse(text = "Mock response for model " + request.model, id = "mock-request", status = "completed", usage = TokenUsage(3, 5))))
    }
}
