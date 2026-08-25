package com.hakayat.backend.jobs

import com.hakayat.backend.data.GenerationJobRecord
import com.hakayat.backend.data.GenerationJobRepository
import java.util.UUID

class GenerationJobWorker(
    private val queue: JobQueue,
    private val repository: GenerationJobRepository,
    private val retryPolicy: RetryPolicy = RetryPolicy(),
    private val processor: suspend (QueuedGenerationJob) -> Unit
) {
    suspend fun runOnce(): Boolean {
        val job = queue.dequeue() ?: return false
        process(job)
        return true
    }

    suspend fun process(job: QueuedGenerationJob) {
        val id = UUID.fromString(job.id)
        try {
            repository.updateStatus(id, "running", 10, attempt = job.attempt)
            processor(job)
            repository.updateStatus(id, "completed", 100, attempt = job.attempt)
        } catch (error: Throwable) {
            val nextAttempt = job.attempt + 1
            val retry = retryPolicy.shouldRetry(nextAttempt)
            repository.updateStatus(id, if (retry) "retrying" else "failed", 0, error.message, nextAttempt)
            if (retry) queue.enqueue(job.copy(attempt = nextAttempt))
        }
    }

    suspend fun ensureJobRecord(job: QueuedGenerationJob) {
        repository.save(
            GenerationJobRecord(
                id = UUID.fromString(job.id),
                projectId = UUID.fromString(job.projectId),
                type = job.type,
                status = "queued",
                attempt = job.attempt
            )
        )
    }
}
