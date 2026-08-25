package com.hakayat.backend.render

import java.util.UUID

interface RenderJobAttemptService {
    suspend fun begin(jobId: UUID): RenderJob?
}

class DefaultRenderJobAttemptService(private val repository: RenderJobRepository) : RenderJobAttemptService {
    override suspend fun begin(jobId: UUID): RenderJob? {
        val current = repository.findById(jobId) ?: return null
        if (current.status == RenderJobStatus.SUCCEEDED) return current
        val next = current.copy(status = RenderJobStatus.RUNNING, progress = 10, attempt = current.attempt + 1, error = null)
        repository.save(next)
        return next
    }
}