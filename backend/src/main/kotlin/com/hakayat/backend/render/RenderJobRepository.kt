package com.hakayat.backend.render

import java.util.UUID

interface RenderJobRepository : RenderJobStore {
    suspend fun findById(id: UUID): RenderJob?
    suspend fun findByProject(projectId: UUID): List<RenderJob>
}