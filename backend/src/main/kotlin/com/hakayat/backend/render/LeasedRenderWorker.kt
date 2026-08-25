package com.hakayat.backend.render

import java.time.Duration
import java.time.Instant

class LeasedRenderWorker(
    private val workerId: String,
    private val queue: RenderJobQueue,
    private val jobs: RenderJobLoader,
    private val timelines: RenderTimelineLoader,
    private val runner: RenderJobRunner,
    private val leaser: RenderJobLeaser,
    private val leaseDuration: Duration = Duration.ofMinutes(10)
) {
    suspend fun processNext(): RenderJob? {
        val id = queue.dequeue() ?: return null
        val lease = leaser.acquire(id, workerId, Instant.now().plus(leaseDuration)) ?: return null
        return try {
            val job = jobs.findById(id) ?: return null
            runner.run(job, timelines.load(job.projectId))
        } finally {
            leaser.release(lease)
        }
    }
}