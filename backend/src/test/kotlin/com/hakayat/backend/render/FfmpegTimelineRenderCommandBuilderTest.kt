package com.hakayat.backend.render

import com.hakayat.backend.media.MediaAssetRecord
import java.io.File
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContains

class FfmpegTimelineRenderCommandBuilderTest {
    @Test
    fun `images are looped for clip duration and timeline is concatenated`() {
        val project = UUID.randomUUID()
        val clip = ResolvedTimelineAsset(
            UUID.randomUUID(),
            MediaAssetRecord(UUID.randomUUID(), project, null, "image", "scene.png", "image/png", "ready"),
            0, 2500, 0
        )
        val command = FfmpegTimelineRenderCommandBuilder().build(listOf(clip), listOf(File("scene.png")), File("out.mp4"))
        assertContains(command, "-loop")
        assertContains(command, "2.5")
        assertContains(command, "concat=n=1:v=1:a=0[vout]")
    }
}
