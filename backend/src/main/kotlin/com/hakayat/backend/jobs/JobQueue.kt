package com.hakayat.backend.jobs

import kotlinx.serialization.Serializable

@Serializable
data class QueuedGenerationJob(
    val id: String,
    val projectId: String,
    val type: String,
    val attempt: Int = 0
)

interface JobQueue {
    suspend fun enqueue(job: QueuedGenerationJob)
    suspend fun dequeue(): QueuedGenerationJob?
}
