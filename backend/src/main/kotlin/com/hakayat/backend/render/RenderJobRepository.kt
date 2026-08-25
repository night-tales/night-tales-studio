package com.hakayat.backend.render

import java.util.UUID

interface RenderJobRepository : RenderJobStore, RenderJobLoader {
    suspend fun findById(id: UUID): RenderJob?
    suspend fun findByProject(projectId: UUID): List<RenderJob>

    override suspend fun find(id: UUID): RenderJob? = findById(id)
    override suspend fun listByProject(projectId: UUID): List<RenderJob> = findByProject(projectId)
}