package com.hakayat.backend.render

import java.util.UUID

class RenderJobStatusService(private val repository: RenderJobRepository) {
    suspend fun snapshot(jobId: UUID): RenderJobProgressSnapshot? = repository.findById(jobId)?.let { RenderJobProgressSnapshot(it.id, it.status, it.progress) }
}