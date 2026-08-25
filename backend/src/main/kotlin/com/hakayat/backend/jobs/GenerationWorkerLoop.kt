package com.hakayat.backend.jobs

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.currentCoroutineContext
import kotlin.time.Duration.Companion.milliseconds

/**
 * Long-running worker loop. Job lifecycle, retries and persistence are owned by
 * GenerationJobWorker; this class only controls polling and cancellation.
 */
class GenerationWorkerLoop(
    private val worker: GenerationJobWorker,
    private val pollDelayMs: Long = 1_000L
) {
    suspend fun run() {
        while (currentCoroutineContext().isActive) {
            try {
                if (!worker.runOnce()) {
                    delay(pollDelayMs.milliseconds)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            }
        }
    }
}
