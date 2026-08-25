package com.hakayat.backend.render

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InMemoryRenderJobStore : RenderJobRepository {
    private val jobs = ConcurrentHashMap<UUID, RenderJob>()

    override suspend fun save(job: RenderJob) {
        jobs[job.id] = job
    }

    override suspend fun find(id: UUID): RenderJob? = jobs[id]

    override suspend fun listByProject(projectId: UUID): List<RenderJob> =
        jobs.values.filter { it.projectId == projectId }.sortedByDescending { it.id.toString() }

    override suspend fun findById(id: UUID): RenderJob? = jobs[id]

    override suspend fun findByProject(projectId: UUID): List<RenderJob> = listByProject(projectId)
}