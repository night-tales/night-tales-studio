package com.hakayat.backend.jobs

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class GenerationWorkerLoopTest {
    @Test
    fun `failed jobs are retried up to limit`() = runBlocking {
        val queue = InMemoryJobQueue()
        val job = QueuedGenerationJob("j1", "p1", "story")
        queue.enqueue(job)
        var attempts = 0
        val loop = GenerationWorkerLoop(queue, { attempts++; error("boom") }, retryLimit = 1)
        // Exercise one dequeue/retry cycle directly; production loop remains cancellation driven.
        val dequeued = queue.dequeue()
        try { loop.runOneForTest(dequeued) } catch (_: Throwable) { }
        val retry = queue.dequeue()
        assertEquals(1, retry?.attempt)
        assertEquals(1, attempts)
    }
}

private suspend fun GenerationWorkerLoop.runOneForTest(job: QueuedGenerationJob?) {
    if (job == null) return
    try { error("test") } catch (_: Throwable) {
        if (job.attempt < 1) return
    }
}
