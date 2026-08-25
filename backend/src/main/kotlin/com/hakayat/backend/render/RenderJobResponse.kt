package com.hakayat.backend.render

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class RenderJobResponse(
    val id: String,
    val projectId: String,
    val status: String,
    val progress: Int,
    val attempt: Int,
    val outputAssetId: String? = null,
    val error: String? = null
) {
    companion object {
        fun from(job: RenderJob) = RenderJobResponse(
            id = job.id.toString(),
            projectId = job.projectId.toString(),
            status = job.status.name,
            progress = job.progress,
            attempt = job.attempt,
            outputAssetId = job.outputAssetId?.toString(),
            error = job.error
        )
    }
}