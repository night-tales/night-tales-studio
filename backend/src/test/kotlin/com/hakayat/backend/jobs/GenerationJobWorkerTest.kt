package com.hakayat.backend.jobs

import com.hakayat.backend.data.GenerationJobRecord
import com.hakayat.backend.data.GenerationJobRepository
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class GenerationJobWorkerTest {
    @Test
    fun `successful job reaches completed state`() = kotlinx.coroutines.test.runTest {
        val queue = InMemoryJobQueue()
        val repository = RecordingRepository()
        val job = QueuedGenerationJob(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "story")
        queue.enqueue(job)
        val worker = GenerationJobWorker(queue, repository) { }
        worker.runOnce()
        assertEquals("completed", repository.status)
        assertEquals(0, repository.attempt)
    }

    @Test
    fun `failed job persists incremented retry attempt`() = kotlinx.coroutines.test.runTest {
        val queue = InMemoryJobQueue()
        val repository = RecordingRepository()
        val job = QueuedGenerationJob(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "story")
        queue.enqueue(job)
        val worker = GenerationJobWorker(queue, repository) { error("temporary failure") }

        worker.runOnce()

        assertEquals("retrying", repository.status)
        assertEquals(1, repository.attempt)
        assertEquals(1, jobAttempt(queue))
    }

    private suspend fun jobAttempt(queue: JobQueue): Int {
        val next = queue.dequeue() ?: error("expected retry job")
        return next.attempt
    }

    private class RecordingRepository : GenerationJobRepository {
        var status = ""
        var attempt = 0

        override suspend fun findById(id: UUID): GenerationJobRecord? = null
        override suspend fun save(job: GenerationJobRecord): GenerationJobRecord = job
        override suspend fun updateStatus(
            id: UUID,
            status: String,
            progress: Int,
            error: String?,
            attempt: Int?
        ) {
            this.status = status
            this.attempt = attempt ?: this.attempt
        }
    }
}
