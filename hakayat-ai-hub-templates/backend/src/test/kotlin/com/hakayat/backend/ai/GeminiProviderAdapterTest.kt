package com.hakayat.backend.ai

import com.hakayat.backend.ai.adapters.GeminiProviderAdapter
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeminiProviderAdapterTest {
    @Test
    fun parsesInteractionPayload() = runBlocking {
        var apiKey: String? = null
        val engine = MockEngine { request ->
            apiKey = request.headers["x-goog-api-key"]
            respond(
                """{"id":"int_123","status":"completed","output_text":"Hello Gemini","usage":{"total_input_tokens":12,"total_output_tokens":8,"total_tokens":20}}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val adapter = GeminiProviderAdapter("test-key", HttpClient(engine))
        val result = adapter.complete(AiRequest("gemini-3.7-flash", "hello", temperature = 0.2, maxTokens = 100))

        assertTrue(result.isSuccess)
        val response = result.getOrThrow()
        assertEquals("test-key", apiKey)
        assertEquals("Hello Gemini", response.text)
        assertEquals(12, response.usage.inputTokens)
        assertEquals(8, response.usage.outputTokens)
        assertEquals("int_123", response.providerRequestId)
    }
}
