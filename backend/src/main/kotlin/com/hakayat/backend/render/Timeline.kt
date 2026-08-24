package com.hakayat.backend.render

import java.util.UUID

data class TimelineClip(
    val id: UUID,
    val assetId: UUID,
    val startMs: Long,
    val durationMs: Long,
    val track: Int = 0
) {
    init {
        require(startMs >= 0) { "startMs must be non-negative" }
        require(durationMs > 0) { "durationMs must be positive" }
        require(track >= 0) { "track must be non-negative" }
    }
}

data class Timeline(val projectId: UUID, val clips: List<TimelineClip>) {
    fun durationMs(): Long = clips.maxOfOrNull { it.startMs + it.durationMs } ?: 0L
    fun orderedClips(): List<TimelineClip> = clips.sortedWith(compareBy<TimelineClip> { it.track }.thenBy { it.startMs })
}
