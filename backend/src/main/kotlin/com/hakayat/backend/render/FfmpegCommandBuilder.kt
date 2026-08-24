package com.hakayat.backend.render

class FfmpegCommandBuilder {
    fun buildVideoCommand(input: String, output: String, durationMs: Long): List<String> = listOf(
        "ffmpeg", "-y", "-i", input,
        "-t", (durationMs / 1000.0).toString(),
        "-c:v", "libx264", "-pix_fmt", "yuv420p", output
    )
}
