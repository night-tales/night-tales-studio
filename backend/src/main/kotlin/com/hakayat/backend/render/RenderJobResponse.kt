package com.hakayat.backend.render

import java.util.UUID

data class RenderJobResponse(
    val id: UUID,
    val projectId: UUID,
    val status: RenderJobStatus,
    val progress: Int,
    val attempt: Int,
    val outputAssetId: UUID?,
    val error: String?
) {
    companion object {
        fun from(job: RenderJob) = RenderJobResponse(
            id = job.id,
            projectId = job.projectId,
            status = job.status,
            progress = job.progress,
            attempt = job.attempt,
            outputAssetId = job.outputAssetId,
            error = job.error
        )
    }
}