package com.hakayat.backend.render

class ObservableRenderJobRunner(
    private val delegate: RenderJobRunner,
    private val events: RenderJobEventPublisher
) {
    suspend fun run(job: RenderJob, timeline: Timeline): RenderJob {
        events.publish(RenderJobEvent(job.id, RenderJobStatus.RUNNING, 10, "render started"))
        return try {
            val result = delegate.run(job, timeline)
            events.publish(RenderJobEvent(result.id, result.status, result.progress, if (result.status == RenderJobStatus.SUCCEEDED) "render completed" else result.error))
            result
        } catch (error: Throwable) {
            events.publish(RenderJobEvent(job.id, RenderJobStatus.FAILED, 100, error.message))
            throw error
        }
    }
}