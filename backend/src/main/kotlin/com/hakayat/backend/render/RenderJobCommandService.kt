package com.hakayat.backend.render

import java.util.UUID

class RenderJobCommandService(
    private val dispatcher: RenderJobDispatcher,
    private val cancellation: RenderJobCancellationService
) {
    suspend fun create(projectId: UUID): RenderJobResponse = RenderJobResponse.from(dispatcher.dispatch(projectId))
    suspend fun cancel(jobId: UUID): RenderJobResponse? = cancellation.cancel(jobId)?.let(RenderJobResponse::from)
}