package com.hakayat.backend.render

import java.time.Instant
import java.util.UUID

data class RenderJobEvent(
    val jobId: UUID,
    val status: RenderJobStatus,
    val progress: Int,
    val message: String? = null,
    val occurredAt: Instant = Instant.now()
) {
    init { require(progress in 0..100) }
}

interface RenderJobEventPublisher {
    suspend fun publish(event: RenderJobEvent)
}