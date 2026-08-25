package com.hakayat.backend.ai

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
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
    fun `creates generation and polls until asset is available`() = runTest {
        var calls = 0
        val client = HttpClient(MockEngine(MockEngineConfig().apply {
            addHandler { request ->
                calls++
                assertEquals("Bearer test-key", request.headers[HttpHeaders.Authorization])
                if (request.url.encodedPath.endsWith("/comics/generations")) {
                    respond(
                        content = "{\"id\":\"gen-1\",\"status\":\"PROCESSING\",\"comics\":[]}",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                } else {
                    respond(
                        content = "{\"id\":\"gen-1\",\"status\":\"PROCESSED\",\"comics\":[{\"page\":0,\"panels\":[{\"panel\":0,\"assetUrl\":\"https://example.test/panel.webp\"}]}]}",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
            }
        }))

        val port = LlamaGenImageGenerationPort(
            apiKey = "test-key",
            client = client,
            pollDelayMs = 0,
            maxPolls = 2
        )

        assertEquals("https://example.test/panel.webp", port.generate("scene-1", "a night city"))
        assertEquals(2, calls)
        client.close()
    }

    @Test
    fun `fails when generation reaches terminal failure`() = runTest {
        val client = HttpClient(MockEngine(MockEngineConfig().apply {
            addHandler {
                respond(
                    content = "{\"id\":\"gen-fail\",\"status\":\"FAILED\",\"comics\":[]}",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            }
        }))

        val port = LlamaGenImageGenerationPort("test-key", client, pollDelayMs = 0)
        assertFailsWith<IllegalStateException> { port.generate("scene-1", "prompt") }
        client.close()
    }
}
