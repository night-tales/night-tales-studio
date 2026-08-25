package com.hakayat.backend.render

import java.util.UUID

class IdempotentRenderJobDispatcher(
    private val dispatcher: RenderJobDispatcher,
    private val idempotency: RenderJobIdempotencyStore,
    private val jobs: RenderJobLoader
) {
    suspend fun dispatch(projectId: UUID, key: String): RenderJob {
        require(key.isNotBlank()) { "idempotency key must not be blank" }
        idempotency.find(projectId, key)?.let { existing ->
            return jobs.findById(existing) ?: error("idempotency record points to missing job")
        }
        val created = dispatcher.dispatch(projectId)
        if (idempotency.bind(projectId, key, created.id)) return created
        val existing = idempotency.find(projectId, key) ?: error("idempotency binding race")
        return jobs.findById(existing) ?: error("idempotency record points to missing job")
    }
}