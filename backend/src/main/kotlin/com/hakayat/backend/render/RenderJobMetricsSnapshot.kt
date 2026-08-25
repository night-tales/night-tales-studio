package com.hakayat.backend.render

data class RenderJobMetricsSnapshot(
    val queued: Long = 0,
    val running: Long = 0,
    val succeeded: Long = 0,
    val failed: Long = 0
)