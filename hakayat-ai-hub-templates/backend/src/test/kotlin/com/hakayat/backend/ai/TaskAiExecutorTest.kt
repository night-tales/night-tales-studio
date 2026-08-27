package com.hakayat.backend.ai

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

private class TestAgent(override val agentId: String) : AiAgentAdapter {
    override suspend fun executeTask(prompt: String): Result<String> = Result.success(prompt)
}

class TaskAiExecutorTest {
    @Test
    fun routes_to_registered_agent() = runBlocking {
        val executor = TaskAiExecutor(
            AiAgentRegistry(listOf(TestAgent("agent-1")))
        )
        assertEquals("hello", executor.execute("agent-1", "hello", "model-1").text)
    }
}
