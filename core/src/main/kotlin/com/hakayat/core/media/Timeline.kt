package com.hakayat.core.media

import kotlinx.serialization.Serializable

@Serializable
data class TimelineClip(
    val id: String,
    val startMs: Long,
    val durationMs: Long,
    val videoUri: String? = null,
    val audioUri: String? = null,
    val subtitleUri: String? = null
)

@Serializable
data class Timeline(val clips: List<TimelineClip> = emptyList()) {
    val durationMs: Long get() = clips.maxOfOrNull { it.startMs + it.durationMs } ?: 0
}

class TimelineBuilder {
    fun build(clips: List<TimelineClip>): Timeline = Timeline(clips.sortedBy { it.startMs })
}
