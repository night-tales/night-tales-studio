package com.hakayat.backend.render

import java.util.UUID

interface RenderJobIdempotencyStore {
    suspend fun find(projectId: UUID, key: String): UUID?
    suspend fun bind(projectId: UUID, key: String, jobId: UUID): Boolean
}

class InMemoryRenderJobIdempotencyStore : RenderJobIdempotencyStore {
    private val values = java.util.concurrent.ConcurrentHashMap<String, UUID>()
    override suspend fun find(projectId: UUID, key: String): UUID? = values["$projectId:$key"]
    override suspend fun bind(projectId: UUID, key: String, jobId: UUID): Boolean =
        values.putIfAbsent("$projectId:$key", jobId) == null
}
