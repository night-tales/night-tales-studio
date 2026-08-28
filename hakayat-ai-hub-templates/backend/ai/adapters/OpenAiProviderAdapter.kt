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
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import com.hakayat.backend.ai.AiStreamEvent
import com.hakayat.backend.ai.parseObject
import com.hakayat.backend.ai.parseSse
import kotlinx.coroutines.flow.Flow

class OpenAiProviderAdapter(
    private val apiKey: String,
    private val client: HttpClient,
    private val endpoint: String = "https://api.openai.com/v1/responses",
    private val json: Json = Json { ignoreUnknownKeys = true }
) : AiProviderAdapter {
    override val provider = AiProvider.OPENAI

    override fun stream(request: AiRequest): Flow<AiStreamEvent> = kotlinx.coroutines.flow.flow {
        if (apiKey.isBlank()) { emit(AiStreamEvent.Failed(IllegalArgumentException("OpenAI API key is not configured"))); return@flow }
        if (request.prompt.isBlank()) { emit(AiStreamEvent.Failed(IllegalArgumentException("Prompt must not be blank"))); return@flow }
        val body = buildJsonObject {
            put("model", request.model)
            put("input", request.prompt)
            put("stream", true)
            request.temperature?.let { put("temperature", it) }
            request.maxTokens?.let { put("max_output_tokens", it) }
        }
        val response = client.post(endpoint) {
            expectSuccess = true
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $apiKey")
            setBody(body)
        }
        var requestId: String? = null
        val text = StringBuilder()
        parseSse(response.bodyAsChannel()) { event, data ->
            val root = json.parseObject(data)
            when (event ?: root["event_type"]?.jsonPrimitive?.content ?: root["type"]?.jsonPrimitive?.content) {
                "response.created" -> { requestId = root["response"]?.jsonObject?.get("id")?.jsonPrimitive?.content; emit(AiStreamEvent.Started(requestId)) }
                "response.output_text.delta" -> root["delta"]?.jsonPrimitive?.content?.let { text.append(it); emit(AiStreamEvent.TextDelta(it)) }
                "response.completed" -> {
                    val r = root["response"]?.jsonObject ?: root
                    val usage = r["usage"]?.jsonObject
                    emit(AiStreamEvent.Completed(AiResponse(text.toString(), AiUsage(usage?.get("input_tokens")?.jsonPrimitive?.long ?: 0, usage?.get("output_tokens")?.jsonPrimitive?.long ?: 0), r["status"]?.jsonPrimitive?.content, r["id"]?.jsonPrimitive?.content ?: requestId)))
                }
                "error" -> emit(AiStreamEvent.Failed(IllegalStateException(data)))
            }
        }
    }

    override suspend fun complete(request: AiRequest): Result<AiResponse> = runCatching {
        require(apiKey.isNotBlank()) { "OpenAI API key is not configured" }
        require(request.prompt.isNotBlank()) { "Prompt must not be blank" }

        val body = buildJsonObject {
            put("model", request.model)
            put("input", request.prompt)
            request.temperature?.let { put("temperature", it) }
            request.maxTokens?.let { put("max_output_tokens", it) }
        }

        val response = client.post(endpoint) {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            setBody(body)
        }

        val root = response.body<String>().let { json.parseToJsonElement(it).jsonObject }
        val text = root["output"]?.jsonArray
            ?.flatMap { item -> item.jsonObject["content"]?.jsonArray?.toList() ?: emptyList() }
            ?.mapNotNull { item -> item.jsonObject["text"]?.jsonPrimitive?.content }
            ?.joinToString("")
            ?: root["output_text"]?.jsonPrimitive?.content
            ?: ""

        AiResponse(
            text = text,
            usage = AiUsage(
                inputTokens = root["usage"]?.jsonObject?.get("input_tokens")?.jsonPrimitive?.long ?: 0,
                outputTokens = root["usage"]?.jsonObject?.get("output_tokens")?.jsonPrimitive?.long ?: 0
            ),
            finishReason = root["status"]?.jsonPrimitive?.content,
            providerRequestId = root["id"]?.jsonPrimitive?.content
        )
    }
}
