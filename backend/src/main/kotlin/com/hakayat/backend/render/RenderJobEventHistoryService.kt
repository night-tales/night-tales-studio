package com.hakayat.backend.render

import java.util.UUID

class RenderJobEventHistoryService(private val log: RenderJobEventLog) {
    suspend fun list(jobId: UUID): List<RenderJobEvent> = log.list(jobId)
}