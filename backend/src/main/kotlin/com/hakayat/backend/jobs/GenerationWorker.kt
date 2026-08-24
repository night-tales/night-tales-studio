package com.hakayat.backend.jobs

import com.hakayat.backend.ai.OrchestratorAgent
import com.hakayat.core.model.JobProgress
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class GenerationWorker(private val orchestrator: OrchestratorAgent) {
    private val progress = ConcurrentHashMap<String, JobProgress>()

    suspend fun execute(projectId: String, prompt: String): JobProgress {
        val jobId = UUID.randomUUID().toString()
        progress[jobId] = JobProgress(jobId, "running", 10, "Planning story")
        val result = orchestrator.generate(prompt)
        progress[jobId] = JobProgress(jobId, "running", 70, "Scenes generated: ${result.second.size}")
        val completed = JobProgress(jobId, "completed", 100, "Blueprint and scenes ready")
        progress[jobId] = completed
        return completed
    }

    fun status(jobId: String): JobProgress? = progress[jobId]
}
