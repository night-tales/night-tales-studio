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
    }

    private class RecordingRepository : GenerationJobRepository {
        var status = ""
        override suspend fun findById(id: UUID): GenerationJobRecord? = null
        override suspend fun save(job: GenerationJobRecord): GenerationJobRecord = job
        override suspend fun updateStatus(id: UUID, status: String, progress: Int, error: String?) {
            this.status = status
        }
    }
}
