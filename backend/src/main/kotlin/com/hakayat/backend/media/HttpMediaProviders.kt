package com.hakayat.backend.media

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

@Serializable
private data class MediaRequest(val input: String, val voice: String? = null)

/** Generic HTTP adapters; provider-specific request/response mapping belongs in concrete deployments. */
class HttpImageGenerator(
    private val client: HttpClient,
    private val endpoint: String,
    private val apiKey: String
) : ImageGenerator {
    override suspend fun generate(prompt: String, width: Int, height: Int): GeneratedMedia {
        val response = client.post(endpoint) {
            contentType(ContentType.Application.Json)
            headers.append("Authorization", "Bearer $apiKey")
            setBody(MediaRequest(prompt))
        }
        return GeneratedMedia(response.headers["X-Asset-Uri"] ?: error("Image provider returned no asset URI"), "image/*")
    }
}

class HttpVoiceGenerator(
    private val client: HttpClient,
    private val endpoint: String,
    private val apiKey: String
) : VoiceGenerator {
    override suspend fun synthesize(text: String, voice: String?): GeneratedMedia {
        val response = client.post(endpoint) {
            contentType(ContentType.Application.Json)
            headers.append("Authorization", "Bearer $apiKey")
            setBody(MediaRequest(text, voice))
        }
        return GeneratedMedia(response.headers["X-Asset-Uri"] ?: error("Voice provider returned no asset URI"), "audio/*")
    }
}

class TemplateSubtitleGenerator : SubtitleGenerator {
    override suspend fun generate(text: String): GeneratedMedia =
        GeneratedMedia("data:text/plain,${java.net.URLEncoder.encode(text, Charsets.UTF_8)}", "text/plain")
}
