package com.hakayat.backend.render

interface RenderWorkerMetrics { fun queued(); fun started(); fun succeeded(); fun failed(); fun retried() }

class NoopRenderWorkerMetrics : RenderWorkerMetrics {
    override fun queued() = Unit
    override fun started() = Unit
    override fun succeeded() = Unit
    override fun failed() = Unit
    override fun retried() = Unit
}