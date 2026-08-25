package com.hakayat.backend.render

import kotlinx.coroutines.delay

class RenderWorkerLoop(
    private val worker: RenderWorker,
    private val idleDelayMs: Long = 250
) {
    init { require(idleDelayMs >= 0) }

    suspend fun runUntilCancelled() {
        while (true) {
            val processed = worker.processNext()
            if (processed == null) delay(idleDelayMs)
        }
    }
}