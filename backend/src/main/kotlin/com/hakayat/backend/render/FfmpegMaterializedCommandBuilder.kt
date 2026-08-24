package com.hakayat.backend.render

import java.io.File

class FfmpegMaterializedCommandBuilder {
    fun build(inputs: MaterializedRenderPlan, output: File): List<String> {
        require(inputs.visual.isNotEmpty()) { "At least one visual input is required" }
        val command = mutableListOf("ffmpeg", "-y")
        inputs.visual.forEach { command.addAll(listOf("-i", it.absolutePath)) }
        inputs.audio.firstOrNull()?.let { command.addAll(listOf("-i", it.absolutePath)) }
        command.addAll(listOf("-map", "0:v:0"))
        if (inputs.audio.isNotEmpty()) {
            command.addAll(listOf("-map", "${inputs.visual.size}:a:0", "-c:a", "aac"))
        }
        command.addAll(listOf("-c:v", "libx264", "-pix_fmt", "yuv420p"))
        inputs.subtitles.firstOrNull()?.let { subtitle ->
            command.addAll(listOf("-vf", "subtitles=${subtitle.absolutePath}"))
        }
        command.add(output.absolutePath)
        return command
    }
}