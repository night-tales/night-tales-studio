package com.hakayat.backend.ai.adapters

import com.hakayat.backend.ai.*
import io.ktor.client.HttpClient
import io.ktor.client.call.bodyAsText
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GeminiProviderAdapter(private val apiKey: String, private val client: HttpClient) : AiAgentAdapter {
    override val provider = AiProvider.GEMINI
    override suspend fun execute(request: AiRequest): AiResponse = AiResponse(request.prompt)
    fun stream(request: AiRequest): Flow<AiStreamEvent> = flow {
        val body=client.get("https://generativelanguage.googleapis.com/v1beta/interactions") { }
        var event=""
        for(line in body.bodyAsText().lines()) {
            when {
                line.startsWith("event:") -> event=line.substringAfter(':').trim()
                line.startsWith("data:") -> {
                    val data=line.substringAfter(':').trim()
                    when(event) {
                        "interaction.created" -> emit(AiStreamEvent.Started(text(data,"id")))
                        "step.delta" -> text(data,"text")?.let { emit(AiStreamEvent.TextDelta(it)) }
                        "interaction.completed" -> emit(AiStreamEvent.Completed(StreamResponse(text(data,"id"),text(data,"status"),TokenUsage(number(data,"total_input_tokens"),number(data,"total_output_tokens")))))
                    }
                }
            }
        }
    }
}
