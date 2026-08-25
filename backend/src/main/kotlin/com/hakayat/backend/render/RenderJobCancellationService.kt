package com.hakayat.backend.render

import java.util.UUID

class RenderJobCancellationService(
    private val repository: RenderJobRepository,
    private val cancellation: RenderCancellationRegistry,
    private val queue: RenderJobQueue
) {
    suspend fun cancel(jobId: UUID): RenderJob? {
        val job = repository.findById(jobId) ?: return null
        if (job.status == RenderJobStatus.SUCCEEDED || job.status == RenderJobStatus.FAILED) return job
        cancellation.cancel(jobId)
        val updated = job.copy(status = RenderJobStatus.FAILED, progress = job.progress, error = "cancelled")
        repository.save(updated)
        return updated
    }
}