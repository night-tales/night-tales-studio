package com.hakayat.backend.data

import java.util.UUID

class InMemoryGenerationJobRepository : GenerationJobRepository {
    private val jobs = linkedMapOf<UUID, GenerationJobRecord>()

    override suspend fun findById(id: UUID): GenerationJobRecord? = jobs[id]

    override suspend fun save(job: GenerationJobRecord): GenerationJobRecord {
        jobs[job.id] = job
        return job
    }

    override suspend fun updateStatus(id: UUID, status: String, progress: Int, error: String?, attempt: Int?) {
        val current = jobs[id] ?: return
        jobs[id] = current.copy(
            status = status,
            progress = progress,
            error = error,
            attempt = attempt ?: current.attempt
        )
    }
}
