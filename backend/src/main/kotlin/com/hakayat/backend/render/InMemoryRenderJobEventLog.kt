package com.hakayat.backend.render

import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

class InMemoryRenderJobEventLog : RenderJobEventLog {
    private val events = CopyOnWriteArrayList<RenderJobEvent>()
    override suspend fun append(event: RenderJobEvent) { events.add(event) }
    override suspend fun list(jobId: UUID): List<RenderJobEvent> = events.filter { it.jobId == jobId }
}