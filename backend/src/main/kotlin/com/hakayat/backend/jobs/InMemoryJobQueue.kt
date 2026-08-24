package com.hakayat.backend.jobs

import kotlinx.coroutines.channels.Channel

/** Deterministic queue adapter for local development and tests. */
class InMemoryJobQueue(capacity: Int = Channel.UNLIMITED) : JobQueue {
    private val channel = Channel<QueuedGenerationJob>(capacity)

    override suspend fun enqueue(job: QueuedGenerationJob) {
        channel.send(job)
    }

    override suspend fun dequeue(): QueuedGenerationJob? = channel.receiveCatching().getOrNull()
}
