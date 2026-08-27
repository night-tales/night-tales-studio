package com.hakayat.backend.ai

import com.hakayat.backend.ai.adapters.OpenAiProviderAdapter
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

class OpenAiProviderAdapterTest {
    @Test
    fun parsesResponsesApiPayload() = runBlocking {
        var authorization: String? = null
        val engine = MockEngine { request ->
            authorization = request.headers[HttpHeaders.Authorization]
            respond(
                """{"id":"resp_123","status":"completed","output_text":"Hello","usage":{"input_tokens":11,"output_tokens":7}}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val adapter = OpenAiProviderAdapter("test-key", HttpClient(engine))
        val result = adapter.complete(AiRequest("gpt-test", "hello"))

        assertTrue(result.isSuccess)
        val response = result.getOrThrow()
        assertEquals("Bearer test-key", authorization)
        assertEquals("Hello", response.text)
        assertEquals(11, response.usage.inputTokens)
        assertEquals(7, response.usage.outputTokens)
        assertEquals("resp_123", response.providerRequestId)
    }
}
