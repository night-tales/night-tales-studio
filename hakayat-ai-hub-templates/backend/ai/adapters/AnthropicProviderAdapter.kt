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
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject\nimport com.hakayat.backend.ai.AiStreamEvent\nimport com.hakayat.backend.ai.parseObject\nimport com.hakayat.backend.ai.parseSse\nimport kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.put

class AnthropicProviderAdapter(
    private val apiKey: String,
    private val client: HttpClient,
    private val endpoint: String = "https://api.anthropic.com/v1/messages",
    private val json: Json = Json { ignoreUnknownKeys = true }
) : AiProviderAdapter {
    override val provider = AiProvider.ANTHROPIC

    override fun stream(request: AiRequest): Flow<AiStreamEvent> = kotlinx.coroutines.flow.flow {
        if (apiKey.isBlank()) { emit(AiStreamEvent.Failed(IllegalArgumentException("Anthropic API key is not configured"))); return@flow }
        if (request.prompt.isBlank()) { emit(AiStreamEvent.Failed(IllegalArgumentException("Prompt must not be blank"))); return@flow }
        val body = buildJsonObject {
            put("model", request.model)
            put("max_tokens", request.maxTokens ?: 1024)
            put("stream", true)
            put("messages", kotlinx.serialization.json.buildJsonArray {
                add(buildJsonObject { put("role", "user"); put("content", request.prompt) })
            })
            request.temperature?.let { put("temperature", it) }
        }
        val response = client.post(endpoint) {
            expectSuccess = true
            contentType(ContentType.Application.Json)
            header("x-api-key", apiKey)
            header("anthropic-version", "2023-06-01")
            setBody(body)
        }
        var requestId: String? = null
        var text = StringBuilder()
        var inputTokens = 0L
        var outputTokens = 0L
        parseSse(response.bodyAsChannel()) { event, data ->
            val root = json.parseObject(data)
            when (event ?: root["type"]?.jsonPrimitive?.content) {
                "message_start" -> {
                    requestId = root["message"]?.jsonObject?.get("id")?.jsonPrimitive?.content
                    inputTokens = root["message"]?.jsonObject?.get("usage")?.jsonObject?.get("input_tokens")?.jsonPrimitive?.long ?: 0
                    emit(AiStreamEvent.Started(requestId))
                }
                "content_block_delta" -> root["delta"]?.jsonObject?.get("text")?.jsonPrimitive?.content?.let {
                    text.append(it); emit(AiStreamEvent.TextDelta(it))
                }
                "message_delta" -> {
                    outputTokens = root["usage"]?.jsonObject?.get("output_tokens")?.jsonPrimitive?.long ?: outputTokens
                }
                "message_stop" -> emit(AiStreamEvent.Completed(AiResponse(text.toString(), AiUsage(inputTokens, outputTokens), providerRequestId = requestId)))
                "error" -> emit(AiStreamEvent.Failed(IllegalStateException(data)))
            }
        }
    }

    override suspend fun complete(request: AiRequest): Result<AiResponse> = runCatching {
        require(apiKey.isNotBlank()) { "Anthropic API key is not configured" }
        require(request.prompt.isNotBlank()) { "Prompt must not be blank" }

        val body = buildJsonObject {
            put("model", request.model)
            put("max_tokens", request.maxTokens ?: 1024)
            put("messages", kotlinx.serialization.json.buildJsonArray {
                add(buildJsonObject {
                    put("role", "user")
                    put("content", request.prompt)
                })
            })
            request.temperature?.let { put("temperature", it) }
        }

        val response = client.post(endpoint) {
            contentType(ContentType.Application.Json)
            header("x-api-key", apiKey)
            header("anthropic-version", "2023-06-01")
            setBody(body)
        }

        val root = json.parseToJsonElement(response.body<String>()).jsonObject
        val text = root["content"]?.jsonArray
            ?.mapNotNull { it.jsonObject["text"]?.jsonPrimitive?.content }
            ?.joinToString("")
            ?: ""

        AiResponse(
            text = text,
            usage = AiUsage(
                inputTokens = root["usage"]?.jsonObject?.get("input_tokens")?.jsonPrimitive?.long ?: 0,
                outputTokens = root["usage"]?.jsonObject?.get("output_tokens")?.jsonPrimitive?.long ?: 0
            ),
            finishReason = root["stop_reason"]?.jsonPrimitive?.content,
            providerRequestId = root["id"]?.jsonPrimitive?.content
        )
    }
}
