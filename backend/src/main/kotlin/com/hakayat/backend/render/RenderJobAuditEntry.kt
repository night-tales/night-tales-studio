package com.hakayat.backend.render

import java.time.Instant
import java.util.UUID

data class RenderJobAuditEntry(val jobId: UUID, val action: String, val actor: String? = null, val occurredAt: Instant = Instant.now())