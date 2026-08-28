package com.hakayat.backend.ai

import com.hakayat.backend.ai.adapters.AnthropicProviderAdapter
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

class AnthropicProviderAdapterTest {
    @Test
    fun parsesMessagesPayload() = runBlocking {
        var apiKey: String? = null
        val engine = MockEngine { request ->
            apiKey = request.headers["x-api-key"]
            respond(
                """{"id":"msg_123","stop_reason":"end_turn","content":[{"type":"text","text":"Hello Claude"}],"usage":{"input_tokens":9,"output_tokens":6}}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val adapter = AnthropicProviderAdapter("test-key", HttpClient(engine))
        val result = adapter.complete(AiRequest("claude-test", "hello"))

        assertTrue(result.isSuccess)
        val response = result.getOrThrow()
        assertEquals("test-key", apiKey)
        assertEquals("Hello Claude", response.text)
        assertEquals(9, response.usage.inputTokens)
        assertEquals(6, response.usage.outputTokens)
        assertEquals("msg_123", response.providerRequestId)
    }
}
