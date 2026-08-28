package com.hakayat.backend.ai.adapters

import com.hakayat.backend.ai.*
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AnthropicProviderAdapter(private val apiKey: String, private val client: HttpClient) : AiAgentAdapter {
    override val provider = AiProvider.ANTHROPIC
    override suspend fun execute(request: AiRequest): AiResponse = AiResponse(request.prompt)
    override fun stream(request: AiRequest): Flow<AiStreamEvent> = flow { emit(AiStreamEvent.Started()) }
}
