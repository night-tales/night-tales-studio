package com.hakayat.backend.jobs

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.currentCoroutineContext
import kotlin.time.Duration.Companion.seconds

class GenerationWorkerLoop(
    private val queue: JobQueue,
    private val handler: suspend (QueuedGenerationJob) -> Unit,
    private val retryLimit: Int = 3
) {
    suspend fun run() {
        while (currentCoroutineContext().isActive) {
            val job = queue.dequeue() ?: run { delay(1.seconds); return@run }
            try {
                handler(job)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Throwable) {
                if (job.attempt < retryLimit) {
                    queue.enqueue(job.copy(attempt = job.attempt + 1))
                }
            }
        }
    }
}
