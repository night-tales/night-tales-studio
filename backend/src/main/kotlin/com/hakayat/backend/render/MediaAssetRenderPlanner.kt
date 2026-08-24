package com.hakayat.backend.render

import com.hakayat.backend.media.MediaAssetRecord

class MediaAssetRenderPlanner {
    fun plan(assets: List<ResolvedTimelineAsset>): RenderPlan {
        val video = assets.filter { it.asset.mimeType.startsWith("video/") || it.asset.mimeType.startsWith("image/") }
        val audio = assets.filter { it.asset.mimeType.startsWith("audio/") }
        val subtitles = assets.filter { it.asset.mimeType.startsWith("text/") }
        require(video.isNotEmpty()) { "Render plan requires at least one visual asset" }
        return RenderPlan(video, audio, subtitles)
    }
}

data class RenderPlan(
    val visual: List<ResolvedTimelineAsset>,
    val audio: List<ResolvedTimelineAsset>,
    val subtitles: List<ResolvedTimelineAsset>
)