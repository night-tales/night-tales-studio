package com.hakayat.backend.ai

interface AiAgentAdapter {
    val provider: AiProvider
    suspend fun execute(request: AiRequest): AiResponse
}
