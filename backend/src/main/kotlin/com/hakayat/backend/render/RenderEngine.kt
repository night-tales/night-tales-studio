package com.hakayat.backend.render

interface RenderEngine {
    suspend fun render(timeline: Timeline, outputKey: String): RenderResult
}

data class RenderResult(val outputUri: String, val durationMs: Long)

class UnconfiguredRenderEngine : RenderEngine {
    override suspend fun render(timeline: Timeline, outputKey: String): RenderResult =
        error("Render engine is not configured")
}
