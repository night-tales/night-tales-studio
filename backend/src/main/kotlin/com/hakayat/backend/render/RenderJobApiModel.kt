package com.hakayat.backend.render

import java.time.Instant
import java.util.UUID

data class RenderJobResponse(
    val id: UUID,
    val projectId: UUID,
    val status: RenderJobStatus,
    val progress: Int,
    val outputAssetId: UUID?,
    val error: String?,
    val updatedAt: Instant = Instant.now()
) {
    companion object {
        fun from(job: RenderJob) = RenderJobResponse(job.id, job.projectId, job.status, job.progress, job.outputAssetId, job.error)
    }
}