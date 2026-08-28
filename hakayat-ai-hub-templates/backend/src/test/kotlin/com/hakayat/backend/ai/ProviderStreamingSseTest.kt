package com.hakayat.backend.ai

import com.hakayat.backend.ai.adapters.AnthropicProviderAdapter
import com.hakayat.backend.ai.adapters.GeminiProviderAdapter
import com.hakayat.backend.ai.adapters.OpenAiProviderAdapter
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProviderStreamingSseTest {
    private fun client(payload: String): HttpClient = HttpClient(MockEngine {
        respond(payload, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "text/event-stream"))
    })

    @Test
    fun openAiSseProducesDeltasAndFinalUsage() = runBlocking {
        val payload = """
            event: response.created
            data: {"response":{"id":"resp_1"}}

            event: response.output_text.delta
            data: {"delta":"Hel"}

            event: response.output_text.delta
            data: {"delta":"lo"}

            event: response.completed
            data: {"response":{"id":"resp_1","status":"completed","usage":{"input_tokens":3,"output_tokens":2}}}

        """.trimIndent()
        val events = OpenAiProviderAdapter("k", client(payload)).stream(AiRequest("m", "p")).toList()
        assertTrue(events[0] is AiStreamEvent.Started)
        assertEquals(listOf("Hel", "lo"), events.filterIsInstance<AiStreamEvent.TextDelta>().map { it.text })
        val done = events.filterIsInstance<AiStreamEvent.Completed>().single()
        assertEquals(3, done.response.usage.inputTokens)
        assertEquals(2, done.response.usage.outputTokens)
    }

    @Test
    fun anthropicSseProducesTextAndUsage() = runBlocking {
        val payload = """
            event: message_start
            data: {"message":{"id":"msg_1","usage":{"input_tokens":4}}}

            event: content_block_delta
            data: {"delta":{"type":"text_delta","text":"Hi"}}

            event: message_delta
            data: {"usage":{"output_tokens":6}}

            event: message_stop
            data: {}

        """.trimIndent()
        val events = AnthropicProviderAdapter("k", client(payload)).stream(AiRequest("m", "p")).toList()
        assertEquals("Hi", events.filterIsInstance<AiStreamEvent.TextDelta>().joinToString("") { it.text })
        assertEquals(6, events.filterIsInstance<AiStreamEvent.Completed>().single().response.usage.outputTokens)
    }

    @Test
    fun geminiSseProducesTextAndFinalUsage() = runBlocking {
        val payload = """
            event: interaction.created
            data: {"interaction":{"id":"int_1","status":"in_progress"}}

            event: step.delta
            data: {"delta":{"type":"text","text":"Hello"},"event_type":"step.delta"}

            event: interaction.completed
            data: {"interaction":{"id":"int_1","status":"completed","usage":{"total_input_tokens":5,"total_output_tokens":7}}}

            event: done
            data: [DONE]

        """.trimIndent()
        val events = GeminiProviderAdapter("k", client(payload)).stream(AiRequest("m", "p")).toList()
        assertEquals("Hello", events.filterIsInstance<AiStreamEvent.TextDelta>().single().text)
        val done = events.filterIsInstance<AiStreamEvent.Completed>().single()
        assertEquals(5, done.response.usage.inputTokens)
        assertEquals(7, done.response.usage.outputTokens)
    }
}
