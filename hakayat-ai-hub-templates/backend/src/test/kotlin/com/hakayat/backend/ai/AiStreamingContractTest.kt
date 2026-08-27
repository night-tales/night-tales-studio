package com.hakayat.backend.ai

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AiStreamingContractTest {
    @Test
    fun default_stream_emits_completed_response() = runBlocking {
        val events = MockAiProviderAdapter().stream(AiRequest("test-model", "hello")).toList()
        assertEquals(1, events.size)
        assertTrue(events.single() is AiStreamEvent.Completed)
        assertEquals("Mock response for model test-model", (events.single() as AiStreamEvent.Completed).response.text)
    }
}
