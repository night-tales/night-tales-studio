package com.hakayat.backend.render

import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

class InMemoryRenderJobAuditLog : RenderJobAuditLog {
    private val entries = CopyOnWriteArrayList<RenderJobAuditEntry>()
    override suspend fun append(entry: RenderJobAuditEntry) { entries.add(entry) }
    override suspend fun list(jobId: UUID): List<RenderJobAuditEntry> = entries.filter { it.jobId == jobId }
}