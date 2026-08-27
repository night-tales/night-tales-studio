package com.hakayat.backend.ai.adapters

import com.hakayat.backend.ai.AiProvider
import com.hakayat.backend.ai.AiProviderAdapter
import com.hakayat.backend.ai.AiRequest
import com.hakayat.backend.ai.AiResponse
import com.hakayat.backend.ai.AiUsage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class GeminiProviderAdapter(
    private val apiKey: String,
    private val client: HttpClient,
    private val endpoint: String = "https://generativelanguage.googleapis.com/v1beta/interactions",
    private val json: Json = Json { ignoreUnknownKeys = true }
) : AiProviderAdapter {
    override val provider = AiProvider.GEMINI

    override suspend fun complete(request: AiRequest): Result<AiResponse> = runCatching {
        require(apiKey.isNotBlank()) { "Gemini API key is not configured" }
        require(request.prompt.isNotBlank()) { "Prompt must not be blank" }

        val body = buildJsonObject {
            put("model", request.model)
            put("input", request.prompt)
            if (request.maxTokens != null || request.temperature != null) {
                put("generation_config", buildJsonObject {
                    request.maxTokens?.let { put("max_output_tokens", it) }
                    request.temperature?.let { put("temperature", it) }
                })
            }
        }

        val response = client.post(endpoint) {
            contentType(ContentType.Application.Json)
            header("x-goog-api-key", apiKey)
            setBody(body)
        }

        val root = json.parseToJsonElement(response.body<String>()).jsonObject
        val text = root["output_text"]?.jsonPrimitive?.content
            ?: root["steps"]?.jsonArray
                ?.flatMap { it.jsonObject["content"]?.jsonArray ?: emptyList() }
                ?.mapNotNull { it.jsonObject["text"]?.jsonPrimitive?.content }
                ?.joinToString("")
            ?: ""

        AiResponse(
            text = text,
            usage = AiUsage(
                inputTokens = root["usage"]?.jsonObject?.get("total_input_tokens")?.jsonPrimitive?.long ?: 0,
                outputTokens = root["usage"]?.jsonObject?.get("total_output_tokens")?.jsonPrimitive?.long ?: 0
            ),
            finishReason = root["status"]?.jsonPrimitive?.content,
            providerRequestId = root["id"]?.jsonPrimitive?.content
        )
    }
}
