package com.hakayat.backend.render

import java.time.Instant
import java.util.UUID

data class RenderJobFailure(
    val jobId: UUID,
    val attempt: Int,
    val message: String,
    val occurredAt: Instant = Instant.now()
)