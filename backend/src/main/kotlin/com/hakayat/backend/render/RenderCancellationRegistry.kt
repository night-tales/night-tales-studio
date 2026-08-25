package com.hakayat.backend.render

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

interface RenderCancellationRegistry { fun cancel(jobId: UUID); fun isCancelled(jobId: UUID): Boolean; fun clear(jobId: UUID) }

class InMemoryRenderCancellationRegistry : RenderCancellationRegistry {
    private val cancelled = ConcurrentHashMap.newKeySet<UUID>()
    override fun cancel(jobId: UUID) { cancelled.add(jobId) }
    override fun isCancelled(jobId: UUID) = cancelled.contains(jobId)
    override fun clear(jobId: UUID) { cancelled.remove(jobId) }
}