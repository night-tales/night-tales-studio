package com.hakayat.backend.render

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FfmpegAudioVideoCommandBuilderTest {
    private val builder = FfmpegAudioVideoCommandBuilder()

    @Test
    fun `builds video audio and subtitle mappings`() {
        val command = builder.build(listOf("scene-1.mp4", "scene-2.mp4"), "narration.wav", "captions.srt", "out.mp4")
        assertEquals("ffmpeg", command.first())
        assertContains(command, "-map")
        assertContains(command, "0:v:0")
        assertContains(command, "2:a:0")
        assertContains(command, "subtitles=captions.srt")
        assertEquals("out.mp4", command.last())
    }

    @Test
    fun `rejects empty video input`() {
        assertFailsWith<IllegalArgumentException> {
            builder.build(emptyList(), null, null, "out.mp4")
        }
    }
}
