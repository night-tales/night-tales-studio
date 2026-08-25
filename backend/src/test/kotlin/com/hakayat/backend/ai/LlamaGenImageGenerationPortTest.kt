package com.hakayat.backend.ai

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LlamaGenImageGenerationPortTest {
    @Test
    fun `creates generation without forcing model and polls until processed asset`() = runTest {
        var calls = 0
        val client = HttpClient(MockEngine(MockEngineConfig().apply {
            addHandler { request ->
                calls++
                assertEquals("Bearer test-key", request.headers[HttpHeaders.Authorization])
                if (request.url.encodedPath.endsWith("/comics/generations")) {
                    assertEquals("POST", request.method.value)
                    respondJson("{\"id\":\"gen-1\",\"status\":\"LOADING\",\"model\":\"cyani-model\"}")
                } else {
                    assertEquals("GET", request.method.value)
                    respondJson("{\"id\":\"gen-1\",\"status\":\"PROCESSED\",\"comics\":[{\"page\":0,\"panels\":[{\"panel\":0,\"assetUrl\":\"https://example.test/panel.webp\"}]}]}")
                }
            }
        }))

        val port = LlamaGenImageGenerationPort("test-key", client, pollDelayMs = 1, maxPolls = 2)
        assertEquals("https://example.test/panel.webp", port.generate("scene-1", "a night city"))
        assertEquals(2, calls)
        client.close()
    }

    @Test
    fun `fails when processed generation has no asset`() = runTest {
        val client = HttpClient(MockEngine(MockEngineConfig().apply {
            addHandler {
                respondJson("{\"id\":\"gen-empty\",\"status\":\"PROCESSED\",\"comics\":[]}")
            }
        }))

        val port = LlamaGenImageGenerationPort("test-key", client, pollDelayMs = 1)
        assertFailsWith<IllegalStateException> { port.generate("scene-1", "prompt") }
        client.close()
    }

    @Test
    fun `fails when generation reaches terminal failure`() = runTest {
        val client = HttpClient(MockEngine(MockEngineConfig().apply {
            addHandler { respondJson("{\"id\":\"gen-fail\",\"status\":\"FAILED\",\"comics\":[]}") }
        }))

        val port = LlamaGenImageGenerationPort("test-key", client, pollDelayMs = 1)
        assertFailsWith<IllegalStateException> { port.generate("scene-1", "prompt") }
        client.close()
    }

    private fun respondJson(body: String) = respond(
        content = body,
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    )
}
