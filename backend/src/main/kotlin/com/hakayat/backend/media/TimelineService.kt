package com.hakayat.backend.media

import com.hakayat.core.model.Timeline
import com.hakayat.core.model.TimelineClip
import com.hakayat.core.model.TimelineTrack
import com.hakayat.core.model.TrackType

class TimelineService {
    fun build(projectId: String, clips: List<TimelineClip>): Timeline {
        val video = clips.filter { it.assetId.startsWith("image:") }
        val audio = clips.filter { it.assetId.startsWith("audio:") }
        return Timeline(
            projectId,
            listOf(
                TimelineTrack("video", TrackType.VIDEO, video),
                TimelineTrack("audio", TrackType.AUDIO, audio),
                TimelineTrack("subtitles", TrackType.SUBTITLE)
            )
        )
    }
}
