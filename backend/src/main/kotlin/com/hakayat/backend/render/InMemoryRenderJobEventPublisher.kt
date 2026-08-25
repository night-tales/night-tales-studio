package com.hakayat.backend.render

import java.util.concurrent.CopyOnWriteArrayList

class InMemoryRenderJobEventPublisher : RenderJobEventPublisher {
    private val events = CopyOnWriteArrayList<RenderJobEvent>()
    override suspend fun publish(event: RenderJobEvent) { events.add(event) }
    fun all(): List<RenderJobEvent> = events.toList()
}