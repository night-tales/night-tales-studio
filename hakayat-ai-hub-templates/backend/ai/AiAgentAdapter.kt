package com.hakayat.backend.ai

/**
 * Compatibility contract for task-level agents.
 * Provider implementations should implement [AiProviderAdapter].
 */
interface AiAgentAdapter {
    val agentId: String
    val providerAdapter: AiProviderAdapter

    suspend fun executeTask(prompt: String): Result<String> {
        val result = providerAdapter.complete(AiRequest(model = agentId, prompt = prompt))
        return result.map { it.text }
    }
}
