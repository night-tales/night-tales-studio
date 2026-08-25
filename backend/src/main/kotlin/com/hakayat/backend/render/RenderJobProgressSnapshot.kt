package com.hakayat.backend.render

import java.time.Instant
import java.util.UUID

data class RenderJobProgressSnapshot(val jobId: UUID, val status: RenderJobStatus, val progress: Int, val updatedAt: Instant = Instant.now()) {
    init { require(progress in 0..100) }
}