package com.hakayat.backend.render

import kotlinx.coroutines.delay
import kotlin.math.min
import java.util.UUID

class RenderRetryScheduler(
    private val queue: RenderJobQueue,
    private val baseDelayMs: Long = 500
) {
    init { require(baseDelayMs >= 0) }
    suspend fun schedule(jobId: UUID, attempt: Int) {
        val delayMs = min(baseDelayMs * (1L shl min(attempt, 10)), 60_000L)
        if (delayMs > 0) delay(delayMs)
        queue.enqueue(jobId)
    }
}