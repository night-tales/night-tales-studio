package com.hakayat.backend.render

class FfmpegMediaComposer(private val commandBuilder: FfmpegCommandBuilder = FfmpegCommandBuilder()) {
    fun commandFor(assetPath: String, outputPath: String, durationMs: Long): List<String> =
        commandBuilder.buildVideoCommand(assetPath, outputPath, durationMs)
}
