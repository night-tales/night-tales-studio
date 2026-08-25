package com.hakayat.backend.render

class RenderEventRecorder(private val log: RenderJobEventLog) : RenderJobEventPublisher {
    override suspend fun publish(event: RenderJobEvent) = log.append(event)
}