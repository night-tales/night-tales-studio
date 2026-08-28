package com.hakayat.backend.ai.adapters

import com.hakayat.backend.ai.*
import io.ktor.client.HttpClient
import io.ktor.client.call.bodyAsText
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AnthropicProviderAdapter(private val apiKey: String, private val client: HttpClient) : AiAgentAdapter {
    override val provider = AiProvider.ANTHROPIC
    override suspend fun execute(request: AiRequest): AiResponse = AiResponse(request.prompt)
    fun stream(request: AiRequest): Flow<AiStreamEvent> = flow {
        val body = client.get("https://api.anthropic.com/v1/messages") { }
        var event = ""
        var input = 0L
        for (line in body.bodyAsText().lines()) {
            when {
                line.startsWith("event:") -> event = line.substringAfter(':').trim()
                line.startsWith("data:") -> {
                    val data=line.substringAfter(':').trim()
                    when(event) {
                        "message_start" -> { input=number(data,"input_tokens"); emit(AiStreamEvent.Started(text(data,"id"))) }
                        "content_block_delta" -> text(data,"text")?.let { emit(AiStreamEvent.TextDelta(it)) }
                        "message_delta" -> input=input
                        "message_stop" -> emit(AiStreamEvent.Completed(StreamResponse(usage=TokenUsage(input,0))))
                    }
                }
            }
        }
    }
}
