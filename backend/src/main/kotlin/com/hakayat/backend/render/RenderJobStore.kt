package com.hakayat.backend.render

import java.util.UUID

interface RenderJobStore {
    suspend fun save(job: RenderJob)
    suspend fun find(id: UUID): RenderJob?
    suspend fun listByProject(projectId: UUID): List<RenderJob>
}