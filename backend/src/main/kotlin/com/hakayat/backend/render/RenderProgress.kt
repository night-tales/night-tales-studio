package com.hakayat.backend.render

import java.util.UUID

enum class RenderStatus { QUEUED, RUNNING, SUCCEEDED, FAILED }

data class RenderProgress(
    val jobId: UUID,
    val status: RenderStatus,
    val percent: Int,
    val message: String? = null
) {
    init { require(percent in 0..100) { "percent must be between 0 and 100" } }
}