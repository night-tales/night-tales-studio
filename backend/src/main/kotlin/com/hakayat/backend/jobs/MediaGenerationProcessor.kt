package com.hakayat.backend.jobs

import com.hakayat.backend.media.MediaAssetRecord
import com.hakayat.backend.media.MediaAssetRepository
import java.util.UUID

class MediaGenerationProcessor(
    private val image: suspend (String, String) -> String,
    private val voice: suspend (String) -> String,
    private val subtitles: suspend (String) -> String,
    private val assets: MediaAssetRepository
) {
    suspend fun process(projectId: UUID, sceneId: UUID, imagePrompt: String, narration: String) {
        val generated = GenerationOutputs(
            image = image(sceneId.toString(), imagePrompt),
            voice = voice(narration),
            subtitles = subtitles(narration)
        )
        assets.save(MediaAssetRecord(UUID.randomUUID(), projectId, sceneId, "image", generated.image, "image/*", "ready"))
        assets.save(MediaAssetRecord(UUID.randomUUID(), projectId, sceneId, "voice", generated.voice, "audio/*", "ready"))
        assets.save(MediaAssetRecord(UUID.randomUUID(), projectId, sceneId, "subtitles", generated.subtitles, "text/plain", "ready"))
    }

    private data class GenerationOutputs(val image: String, val voice: String, val subtitles: String)
}
