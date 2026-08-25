package com.hakayat.backend.render

import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InMemoryRenderJobLeaser : RenderJobLeaser {
    private val leases = ConcurrentHashMap<UUID, RenderJobLease>()
    override suspend fun acquire(jobId: UUID, workerId: String, until: Instant): RenderJobLease? {
        val current = leases[jobId]
        if (current != null && current.expiresAt.isAfter(Instant.now())) return null
        val lease = RenderJobLease(jobId, workerId, until)
        leases[jobId] = lease
        return lease
    }
    override suspend fun release(lease: RenderJobLease) { leases.remove(lease.jobId, lease) }
}