package com.hakayat.backend.render

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FfmpegRenderEngine(
    private val ffmpegBinary: String = "ffmpeg",
    private val workDirectory: File = File(System.getProperty("java.io.tmpdir"), "night-tales-render")
) : RenderEngine {
    override suspend fun render(timeline: Timeline, outputKey: String): RenderResult = withContext(Dispatchers.IO) {
        require(timeline.clips.isNotEmpty()) { "Cannot render an empty timeline" }
        workDirectory.mkdirs()
        val output = File(workDirectory, outputKey)
        val command = listOf(ffmpegBinary, "-y", "-f", "lavfi", "-i", "color=c=black:s=1280x720:r=30", "-t", "${timeline.durationMs() / 1000.0}", "-c:v", "libx264", output.absolutePath)
        val process = ProcessBuilder(command).redirectErrorStream(true).start()
        val log = process.inputStream.bufferedReader().use { it.readText() }
        val exit = process.waitFor()
        if (exit != 0) error("FFmpeg render failed (exit=$exit): $log")
        RenderResult(output.toURI().toString(), timeline.durationMs())
    }
}
