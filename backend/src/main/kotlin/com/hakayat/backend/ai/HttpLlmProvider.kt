package com.hakayat.backend.ai

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
private data class ChatRequest(val model: String, val messages: List<Message>, val temperature: Double)

@Serializable
private data class Message(val role: String, val content: String)

@Serializable
private data class ChatResponse(val choices: List<Choice> = emptyList())

@Serializable
private data class Choice(val message: Message)

class HttpLlmProvider(
    private val client: HttpClient,
    private val endpoint: String,
    private val apiKey: String,
    private val model: String
) : LlmProvider {
    override val name: String = "http-llm"

    override suspend fun complete(request: LlmRequest): String {
        val response: ChatResponse = client.post(endpoint) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $apiKey")
            setBody(ChatRequest(model, listOf(Message("system", request.system), Message("user", request.prompt)), request.temperature))
        }.body()
        return response.choices.firstOrNull()?.message?.content
            ?: error("LLM returned no completion")
    }
}
