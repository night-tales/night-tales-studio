package com.hakayat.backend.render

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InMemoryRenderIdempotencyStore : RenderIdempotencyStore {
    private val values = ConcurrentHashMap<String, UUID>()
    private fun key(projectId: UUID, idempotencyKey: String) = "$projectId:$idempotencyKey"
    override suspend fun find(projectId: UUID, key: String): UUID? = values[key(projectId, key)]
    override suspend fun put(projectId: UUID, key: String, jobId: UUID): Boolean = values.putIfAbsent(key(projectId, key), jobId) == null
}