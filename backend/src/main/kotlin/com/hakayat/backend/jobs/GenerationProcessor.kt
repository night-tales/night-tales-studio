package com.hakayat.backend.jobs

import com.hakayat.backend.ai.LlmProvider
import com.hakayat.backend.ai.LlmRequest

class GenerationProcessor(
    private val llm: LlmProvider
) {
    suspend fun process(job: QueuedGenerationJob) {
        when (job.type) {
            "story", "blueprint" -> llm.complete(
                LlmRequest(
                    system = "You are a story planning agent for Night Tales Studio.",
                    prompt = "Generate a structured plan for project ${job.projectId}."
                )
            )
            else -> error("Unsupported generation type: ${job.type}")
        }
    }
}
