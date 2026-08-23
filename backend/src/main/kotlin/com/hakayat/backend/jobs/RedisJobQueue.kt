package com.hakayat.backend.jobs

import kotlinx.serialization.Serializable

@Serializable
data class QueuedGenerationJob(val id: String, val projectId: String, val type: String, val attempt: Int = 0)

interface JobQueue {
    suspend fun enqueue(job: QueuedGenerationJob)
    suspend fun dequeue(): QueuedGenerationJob?
}

/** Contract only: production Redis implementation is injected by infrastructure. */
class RedisJobQueue : JobQueue {
    override suspend fun enqueue(job: QueuedGenerationJob) = Unit
    override suspend fun dequeue(): QueuedGenerationJob? = null
}
