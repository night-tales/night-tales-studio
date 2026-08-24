package com.hakayat.backend.render

import com.hakayat.backend.media.MediaAssetRecord
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MediaAssetRenderPlannerTest {
    @Test
    fun `classifies visual audio and subtitle assets`() {
        val project = UUID.randomUUID()
        val assets = listOf(
            ResolvedTimelineAsset(UUID.randomUUID(), MediaAssetRecord(UUID.randomUUID(), project, null, "image", "scene.png", "image/png", "ready"), 0, 2000, 0),
            ResolvedTimelineAsset(UUID.randomUUID(), MediaAssetRecord(UUID.randomUUID(), project, null, "voice", "voice.aac", "audio/aac", "ready"), 0, 2000, 1),
            ResolvedTimelineAsset(UUID.randomUUID(), MediaAssetRecord(UUID.randomUUID(), project, null, "subtitles", "captions.srt", "text/srt", "ready"), 0, 2000, 2)
        )
        val plan = MediaAssetRenderPlanner().plan(assets)
        assertEquals(1, plan.visual.size)
        assertEquals(1, plan.audio.size)
        assertEquals(1, plan.subtitles.size)
    }

    @Test
    fun `rejects plans without visual media`() {
        val project = UUID.randomUUID()
        val asset = ResolvedTimelineAsset(UUID.randomUUID(), MediaAssetRecord(UUID.randomUUID(), project, null, "voice", "voice.aac", "audio/aac", "ready"), 0, 1000, 0)
        assertFailsWith<IllegalArgumentException> { MediaAssetRenderPlanner().plan(listOf(asset)) }
    }
}
