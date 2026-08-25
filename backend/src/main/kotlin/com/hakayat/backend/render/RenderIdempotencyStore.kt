package com.hakayat.backend.render

import java.util.UUID

interface RenderIdempotencyStore {
    suspend fun find(projectId: UUID, key: String): UUID?
    suspend fun put(projectId: UUID, key: String, jobId: UUID): Boolean
}
