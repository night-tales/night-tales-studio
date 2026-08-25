package com.hakayat.backend.render

import java.util.UUID

class RenderJobApiService(
    private val dispatcher: RenderJobDispatcher,
    private val query: RenderJobQueryService,
    private val idempotentDispatcher: IdempotentRenderJobDispatcher? = null
) {
    suspend fun create(projectId: UUID, idempotencyKey: String? = null): RenderJobResponse {
        val job = if (idempotencyKey != null && idempotentDispatcher != null) {
            idempotentDispatcher.dispatch(projectId, idempotencyKey)
        } else dispatcher.dispatch(projectId)
        return RenderJobResponse.from(job)
    }

    suspend fun get(jobId: UUID): RenderJobResponse? = query.get(jobId)
    suspend fun list(projectId: UUID): List<RenderJobResponse> = query.list(projectId)
}