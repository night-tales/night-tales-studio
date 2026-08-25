package com.hakayat.backend.render

import java.time.Instant
import java.util.UUID

data class RenderJobLease(val jobId: UUID, val workerId: String, val expiresAt: Instant)
