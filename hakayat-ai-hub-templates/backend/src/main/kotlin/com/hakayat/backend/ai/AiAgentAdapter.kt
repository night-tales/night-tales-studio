package com.hakayat.backend.ai

interface AiAgentAdapter {
    val provider: AiProvider
    val agentId: String get() = provider.name.lowercase()

    suspend fun execute(request: AiRequest): AiResponse

    suspend fun complete(request: AiRequest): Result<AiResponse> = runCatching {
        require(request.prompt.isNotBlank()) { "Prompt must not be blank" }
        execute(request)
    }

    suspend fun executeTask(prompt: String): Result<String> =
        complete(AiRequest(model = "default", prompt = prompt)).map { it.text }

    fun stream(request: AiRequest): kotlinx.coroutines.flow.Flow<AiStreamEvent> =
        kotlinx.coroutines.flow.flow {
            complete(request).onSuccess { response ->
                emit(
                    AiStreamEvent.Completed(
                        StreamResponse(
                            text = response.text,
                            id = response.providerRequestId,
                            usage = response.usage
                        )
                    )
                )
            }.onFailure { throw it }
        }
}
