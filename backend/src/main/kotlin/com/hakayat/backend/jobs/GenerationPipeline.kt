package com.hakayat.backend.jobs

import com.hakayat.core.model.GenerationJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

interface ImageGenerationPort { suspend fun generate(sceneId: String, prompt: String): String }
interface VoiceGenerationPort { suspend fun generate(sceneId: String, text: String): String }
interface SubtitleGenerationPort { suspend fun generate(sceneId: String, text: String): String }

class GenerationPipeline(
    private val image: ImageGenerationPort,
    private val voice: VoiceGenerationPort,
    private val subtitles: SubtitleGenerationPort
) {
    suspend fun run(job: GenerationJob, sceneId: String, imagePrompt: String, narration: String): Map<String, String> = coroutineScope {
        val imageJob = async { image.generate(sceneId, imagePrompt) }
        val voiceJob = async { voice.generate(sceneId, narration) }
        val subtitleJob = async { subtitles.generate(sceneId, narration) }
        mapOf(
            "image" to imageJob.await(),
            "voice" to voiceJob.await(),
            "subtitles" to subtitleJob.await()
        )
    }
}
