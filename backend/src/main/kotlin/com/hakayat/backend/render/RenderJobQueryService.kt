package com.hakayat.backend.render

import java.util.UUID

class RenderJobQueryService(private val repository: RenderJobRepository) {
    suspend fun get(id: UUID): RenderJobResponse? = repository.findById(id)?.let(RenderJobResponse::from)

    suspend fun list(projectId: UUID): List<RenderJobResponse> =
        repository.findByProject(projectId).map(RenderJobResponse::from)
}