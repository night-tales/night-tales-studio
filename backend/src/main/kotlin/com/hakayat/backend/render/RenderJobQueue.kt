package com.hakayat.backend.render

import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue

interface RenderJobQueue {
    suspend fun enqueue(jobId: UUID)
    suspend fun dequeue(): UUID?
}

class InMemoryRenderJobQueue : RenderJobQueue {
    private val queue = ConcurrentLinkedQueue<UUID>()
    override suspend fun enqueue(jobId: UUID) { queue.add(jobId) }
    override suspend fun dequeue(): UUID? = queue.poll()
}