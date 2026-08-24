package com.hakayat.backend.render

import java.util.UUID

class RenderJobDispatcher(
    private val jobs: RenderJobStore,
    private val queue: RenderJobQueue
) {
    suspend fun dispatch(projectId: UUID): RenderJob {
        val job = RenderJob(UUID.randomUUID(), projectId)
        jobs.save(job)
        queue.enqueue(job.id)
        return job
    }
}