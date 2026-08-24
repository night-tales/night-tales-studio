package com.hakayat.backend.render

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class FfmpegRenderEngine(
    private val ffmpegBinary: String = "ffmpeg",
    private val workDirectory: File = File(System.getProperty("java.io.tmpdir"), "night-tales-render"),
    private val commandBuilder: FfmpegTimelineCommandBuilder = FfmpegTimelineCommandBuilder(),
    private val materializer: AssetMaterializer = LocalAssetMaterializer(),
    private val assetResolver: MediaAssetResolver
) : RenderEngine {
    override suspend fun render(timeline: Timeline, outputKey: String): RenderResult = withContext(Dispatchers.IO) {
        require(timeline.clips.isNotEmpty()) { "Cannot render an empty timeline" }
        workDirectory.mkdirs()
        val output = File(workDirectory, outputKey)
        val inputs = timeline.orderedClips().mapIndexed { index, clip ->
            val uri = assetResolver.resolve(timeline.projectId, clip.assetId)
            val destination = File(workDirectory, "asset-${index}-${clip.assetId}.media")
            materializer.materialize(uri, destination).absolutePath
        }
        val built = commandBuilder.build(inputs, output.absolutePath, timeline.durationMs())
        val command = built.toMutableList().also { it[0] = ffmpegBinary }
        val process = ProcessBuilder(command).redirectErrorStream(true).start()
        val log = process.inputStream.bufferedReader().use { it.readText() }
        val exit = process.waitFor()
        if (exit != 0) error("FFmpeg render failed (exit=$exit): $log")
        require(output.isFile && output.length() > 0) { "FFmpeg produced no output" }
        RenderResult(output.toURI().toString(), timeline.durationMs())
    }
}
