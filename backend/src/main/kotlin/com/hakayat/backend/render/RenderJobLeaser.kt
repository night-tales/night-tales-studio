package com.hakayat.backend.render

import java.time.Instant
import java.util.UUID

interface RenderJobLeaser {
    suspend fun acquire(jobId: UUID, workerId: String, until: Instant): RenderJobLease?
    suspend fun release(lease: RenderJobLease)
}
