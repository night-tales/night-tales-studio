package com.hakayat.backend.render

import java.util.UUID

interface RenderJobEventLog {
    suspend fun append(event: RenderJobEvent)
    suspend fun list(jobId: UUID): List<RenderJobEvent>
}