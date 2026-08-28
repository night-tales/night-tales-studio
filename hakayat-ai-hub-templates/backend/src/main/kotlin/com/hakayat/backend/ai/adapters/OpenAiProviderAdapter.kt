package com.hakayat.backend.ai.adapters

import com.hakayat.backend.ai.*
import io.ktor.client.HttpClient
import io.ktor.client.call.bodyAsText
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class OpenAiProviderAdapter(private val apiKey: String, private val client: HttpClient) : AiAgentAdapter {
    override val provider = AiProvider.OPENAI
    override suspend fun execute(request: AiRequest): AiResponse = AiResponse(request.prompt)
    fun stream(request: AiRequest): Flow<AiStreamEvent> = flow {
        val body = client.get("https://api.openai.com/v1/responses") { }
        var event = ""
        for (line in body.bodyAsText().lines()) {
            when {
                line.startsWith("event:") -> event = line.substringAfter(':').trim()
                line.startsWith("data:") -> {
                    val data = line.substringAfter(':').trim()
                    when (event) {
                        "response.created" -> emit(AiStreamEvent.Started(text(data, "id")))
                        "response.output_text.delta" -> text(data, "delta")?.let { emit(AiStreamEvent.TextDelta(it)) }
                        "response.completed" -> emit(AiStreamEvent.Completed(StreamResponse(text(data, "id"), text(data, "status"), TokenUsage(number(data, "input_tokens"), number(data, "output_tokens")))))
                    }
                }
            }
        }
    }
}
