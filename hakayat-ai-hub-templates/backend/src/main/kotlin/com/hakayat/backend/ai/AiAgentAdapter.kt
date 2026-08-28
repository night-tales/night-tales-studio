package com.hakayat.backend.ai

interface AiAgentAdapter {
    val provider: AiProvider
    val agentId: String get() = provider.name.lowercase()

    suspend fun execute(request: AiRequest): AiResponse =\n        executeTask(request.prompt).map { AiResponse(text = it) }.getOrThrow()

    suspend fun complete(request: AiRequest): Result<AiResponse> = runCatching {
        require(request.prompt.isNotBlank()) { "Prompt must not be blank" }
        execute(request)
    }

    suspend fun executeTask(prompt: String): Result<String> =
        complete(AiRequest(model = "default", prompt = prompt)).map { it.text }

    fun stream(request: AiRequest): kotlinx.coroutines.flow.Flow<AiStreamEvent> =
        kotlinx.coroutines.flow.flow {
            complete(request).onSuccess {
                emit(AiStreamEvent.Completed(StreamResponse(text = it.text, id = it.providerRequestId, usage = it.usage)))
            }.onFailure { throw it }
        }
}
