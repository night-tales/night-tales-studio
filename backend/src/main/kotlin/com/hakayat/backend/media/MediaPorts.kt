package com.hakayat.backend.media

interface ImageGenerator {
    suspend fun generate(prompt: String, width: Int = 1280, height: Int = 720): GeneratedMedia
}

interface VoiceGenerator {
    suspend fun synthesize(text: String, voice: String? = null): GeneratedMedia
}

interface SubtitleGenerator {
    suspend fun generate(text: String): GeneratedMedia
}

data class GeneratedMedia(
    val uri: String,
    val contentType: String,
    val durationMs: Long? = null
)

class UnconfiguredImageGenerator : ImageGenerator {
    override suspend fun generate(prompt: String, width: Int, height: Int) =
        error("Image provider is not configured")
}

class UnconfiguredVoiceGenerator : VoiceGenerator {
    override suspend fun synthesize(text: String, voice: String?) =
        error("Voice provider is not configured")
}

class UnconfiguredSubtitleGenerator : SubtitleGenerator {
    override suspend fun generate(text: String) =
        error("Subtitle generator is not configured")
}
