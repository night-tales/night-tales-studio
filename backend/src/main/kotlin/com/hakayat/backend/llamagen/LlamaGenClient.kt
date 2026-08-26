package com.hakayat.backend.llamagen

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.delay

class LlamaGenClient(
    private val http: HttpClient,
    private val apiKey: String,
    private val baseUrl: String = "https://api.llamagen.ai",
    private val pollDelayMs: Long = 3_000,
    private val timeoutMs: Long = 10 * 60 * 1_000
) {
    suspend fun create(request: LlamaGenCreateRequest): LlamaGenGenerationResponse =
        http.post("$baseUrl/v1/comics/generations") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            setBody(request)
        }.body()

    suspend fun get(id: String): LlamaGenGenerationResponse =
        http.get("$baseUrl/v1/comics/generations/$id") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
        }.body()

    suspend fun waitForCompletion(id: String): LlamaGenGenerationResponse {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val response = get(id)
            when (response.status) {
                LlamaGenStatus.PROCESSED,
                LlamaGenStatus.FAILED,
                LlamaGenStatus.CANCELLED -> return response
                LlamaGenStatus.QUEUED,
                LlamaGenStatus.PROCESSING -> delay(pollDelayMs)
            }
        }
        error("LlamaGen generation timed out: $id")
    }
}
