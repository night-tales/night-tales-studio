package com.hakayat.backend.ai

data class AiRequest(
    val model: String,
    val prompt: String,
    val temperature: Double? = null,
    val maxTokens: Int? = null,
)
