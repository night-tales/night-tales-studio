package com.hakayat.backend.ai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

class TaskAiExecutor(
    private val registry: AiAgentRegistry,
    private val policy: ProviderExecutionPolicy = ProviderExecutionPolicy()
) {
    suspend fun execute(
        agentId: String,
        prompt: String,
        model: String = agentId
    ): AiResponse {
        val adapter = registry.require(agentId)
        val request = AiRequest(model = model, prompt = prompt)
        return executeWithPolicy(policy) {
            adapter.complete(request).getOrElse { throw it }
        }
    }
}
