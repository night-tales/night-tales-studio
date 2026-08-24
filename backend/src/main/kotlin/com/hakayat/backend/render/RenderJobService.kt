package com.hakayat.backend.render

import java.util.UUID

class RenderJobService(private val store: RenderJobStore) {
    suspend fun create(projectId: UUID): RenderJob {
        val job = RenderJob(UUID.randomUUID(), projectId)
        store.save(job)
        return job
    }

    fun id(job: RenderJob): UUID = job.id
}