package com.hakayat.backend.render

import com.hakayat.backend.media.MediaAssetRecord
import java.util.UUID

data class ResolvedTimelineAsset(
    val clipId: UUID,
    val asset: MediaAssetRecord,
    val startMs: Long,
    val durationMs: Long,
    val track: Int
)

class TimelineAssetResolver(private val resolver: MediaAssetResolver) {
    suspend fun resolve(timeline: Timeline): List<ResolvedTimelineAsset> = timeline.orderedClips().map { clip ->
        val asset = resolver.resolveRecord(timeline.projectId, clip.assetId)
        ResolvedTimelineAsset(clip.id, asset, clip.startMs, clip.durationMs, clip.track)
    }
}
