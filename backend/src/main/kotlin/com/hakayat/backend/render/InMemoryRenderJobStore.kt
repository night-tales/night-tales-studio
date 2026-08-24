package com.hakayat.backend.render

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InMemoryRenderJobStore : RenderJobStore {
    private val jobs = ConcurrentHashMap<UUID, RenderJob>()

    override suspend fun save(job: RenderJob) {
        jobs[job.id] = job
    }

    fun find(id: UUID): RenderJob? = jobs[id]
}