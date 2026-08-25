package com.hakayat.backend.render

import java.util.UUID

interface RenderJobLoader {
    suspend fun findById(id: UUID): RenderJob?
}

interface RenderTimelineLoader {
    suspend fun load(projectId: UUID): Timeline
}

class RenderWorker(
    private val queue: RenderJobQueue,
    private val jobs: RenderJobLoader,
    private val timelines: RenderTimelineLoader,
    private val runner: RenderJobRunner
) {
    suspend fun processNext(): RenderJob? {
        val id = queue.dequeue() ?: return null
        val job = jobs.findById(id) ?: return null
        val timeline = timelines.load(job.projectId)
        return runner.run(job, timeline)
    }
}