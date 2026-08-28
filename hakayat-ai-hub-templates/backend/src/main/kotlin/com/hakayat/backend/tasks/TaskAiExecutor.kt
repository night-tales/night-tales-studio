package com.hakayat.backend.tasks

import com.hakayat.backend.ai.AiAgentRegistry
import com.hakayat.backend.ai.AiRequest
import com.hakayat.backend.ai.ProviderExecutionPolicy
import com.hakayat.backend.usage.UsageRecord

class TaskAiExecutor(
    private val registry: AiAgentRegistry,
) {
    suspend fun execute(
        request: AiRequest,
        policy: ProviderExecutionPolicy,
    ): ExecutionResult {
        var lastFailure: Throwable? = null
        for (provider in policy.providersInOrder()) {
            try {
                val response = registry.get(provider).execute(request)
                return ExecutionResult(response.text, provider, null)
            } catch (t: Throwable) {
                lastFailure = t
            }
        }
        throw lastFailure ?: IllegalStateException("No AI provider configured")
    }

    data class ExecutionResult(
        val text: String,
        val provider: com.hakayat.backend.ai.AiProvider,
        val usage: UsageRecord?,
    )
}
