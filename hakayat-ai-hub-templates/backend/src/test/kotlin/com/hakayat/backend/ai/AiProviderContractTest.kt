package com.hakayat.backend.ai

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AiProviderContractTest {
    @Test
    fun providerReturnsNormalizedResponseAndUsage() = runBlocking {
        val adapter = MockAiProviderAdapter()
        val result = adapter.complete(
            AiRequest(model = "test-model", prompt = "hello")
        )

        assertTrue(result.isSuccess)
        val response = result.getOrThrow()
        assertEquals(AiProvider.OPENAI, adapter.provider)
        assertEquals("stop", response.finishReason)
        assertEquals(5, response.usage.outputTokens)
        assertTrue(response.usage.totalTokens > response.usage.outputTokens)
    }

    @Test
    fun blankPromptIsRejected() = runBlocking {
        val result = MockAiProviderAdapter().complete(
            AiRequest(model = "test-model", prompt = " ")
        )

        assertTrue(result.isFailure)
    }
}
