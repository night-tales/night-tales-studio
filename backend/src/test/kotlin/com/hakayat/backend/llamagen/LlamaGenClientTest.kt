package com.hakayat.backend.llamagen

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteReadPacket
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LlamaGenClientTest {
    @Test
    fun `create sends bearer auth and decodes generation`() = runTest {
        val engine = MockEngine(MockEngineConfig().apply {
            addHandler { request ->
                assertEquals("Bearer test-key", request.headers[HttpHeaders.Authorization])
                assertEquals("POST", request.method.value)
                respond(
                    content = "{\"id\":\"gen-1\",\"status\":\"QUEUED\"}",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            }
        })
        HttpClient(engine) {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(kotlinx.serialization.json.Json { ignoreUnknownKeys = true })
            }
        }.use { http ->
            val result = LlamaGenClient(http, "test-key").create(LlamaGenCreateRequest(prompt = "hello"))
            assertEquals("gen-1", result.id)
            assertEquals(LlamaGenStatus.QUEUED, result.status)
        }
    }

    @Test
    fun `waitForCompletion polls until processed and reads asset`() = runTest {
        var calls = 0
        val engine = MockEngine(MockEngineConfig().apply {
            addHandler {
                calls++
                val body = if (calls == 1)
                    "{\"id\":\"gen-2\",\"status\":\"PROCESSING\"}"
                else
                    "{\"id\":\"gen-2\",\"status\":\"PROCESSED\",\"data\":{\"assetUrl\":\"https://cdn.example/p.webp\"}}"
                respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
            }
        })
        HttpClient(engine) {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(kotlinx.serialization.json.Json { ignoreUnknownKeys = true })
            }
        }.use { http ->
            val result = LlamaGenClient(http, "test-key", pollDelayMs = 1, timeoutMs = 1000).waitForCompletion("gen-2")
            assertEquals(LlamaGenStatus.PROCESSED, result.status)
            assertEquals("https://cdn.example/p.webp", result.data?.assetUrl)
            assertTrue(calls >= 2)
        }
    }

    @Test
    fun `waitForCompletion returns failed generation`() = runTest {
        val engine = MockEngine(MockEngineConfig().apply {
            addHandler {
                respond("{\"id\":\"gen-3\",\"status\":\"FAILED\",\"error\":{\"code\":\"invalid_request\",\"message\":\"bad prompt\"}}", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
            }
        })
        HttpClient(engine) {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(kotlinx.serialization.json.Json { ignoreUnknownKeys = true })
            }
        }.use { http ->
            val result = LlamaGenClient(http, "test-key").waitForCompletion("gen-3")
            assertEquals(LlamaGenStatus.FAILED, result.status)
            assertEquals("invalid_request", result.error?.code)
        }
    }
}
