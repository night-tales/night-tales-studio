package com.hakayat.backend.render

import java.util.UUID

enum class RenderJobStatus { QUEUED, RUNNING, SUCCEEDED, FAILED }

data class RenderJob(
    val id: UUID,
    val projectId: UUID,
    val status: RenderJobStatus = RenderJobStatus.QUEUED,
    val progress: Int = 0,
    val outputAssetId: UUID? = null,
    val error: String? = null,
    val attempt: Int = 0
) {
    init {
        require(progress in 0..100) { "progress must be between 0 and 100" }
        require(attempt >= 0) { "attempt must be non-negative" }
    }
}