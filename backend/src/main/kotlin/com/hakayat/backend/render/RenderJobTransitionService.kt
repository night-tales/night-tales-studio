package com.hakayat.backend.render

class RenderJobTransitionService(private val repository: RenderJobRepository) {
    suspend fun transition(job: RenderJob, target: RenderJobStatus): RenderJob {
        require(RenderJobTransitions.allowed(job.status, target)) { "invalid render transition: ${job.status} -> $target" }
        val next = job.copy(status = target)
        repository.save(next)
        return next
    }
}