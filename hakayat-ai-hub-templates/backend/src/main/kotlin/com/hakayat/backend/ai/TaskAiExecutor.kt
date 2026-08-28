package com.hakayat.backend.ai

class TaskAiExecutor(private val registry: AiAgentRegistry) {
    suspend fun execute(agentId: String, prompt: String, model: String): ExecutionResult {
        val adapter = registry.resolve(agentId)
        val response = adapter.execute(AiRequest(model = model, prompt = prompt))
        return ExecutionResult(response.text)
    }

    data class ExecutionResult(val text: String)
}
