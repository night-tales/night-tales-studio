package com.hakayat.backend.render

import java.util.UUID

interface RenderJobAuditLog { suspend fun append(entry: RenderJobAuditEntry); suspend fun list(jobId: UUID): List<RenderJobAuditEntry> }