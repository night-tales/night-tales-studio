package com.hakayat.backend.render

import java.util.UUID

interface RenderJobStore {
    suspend fun save(job: RenderJob)
}

class RenderJobRunner(
    private val store: RenderJobStore,
    private val outputService: RenderOutputService
) {
    suspend fun run(job: RenderJob, timeline: Timeline): RenderJob {
        var current = job.copy(status = RenderJobStatus.RUNNING, progress = 10, error = null)
        store.save(current)
        return try {
            current = current.copy(progress = 50)
            store.save(current)
            val asset = outputService.renderAndStore(timeline, "renders/${job.projectId}/${job.id}.mp4")
            current.copy(status = RenderJobStatus.SUCCEEDED, progress = 100, outputAssetId = asset.id).also { store.save(it) }
        } catch (error: Throwable) {
            current.copy(status = RenderJobStatus.FAILED, progress = 100, error = error.message ?: error::class.simpleName).also { store.save(it) }
        }
    }
}