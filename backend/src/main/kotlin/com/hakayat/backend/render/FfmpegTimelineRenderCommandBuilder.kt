package com.hakayat.backend.render

import java.io.File

class FfmpegTimelineRenderCommandBuilder {
    fun build(clips: List<ResolvedTimelineAsset>, files: List<File>, output: File): List<String> {
        require(clips.isNotEmpty()) { "Timeline requires at least one clip" }
        require(clips.size == files.size) { "Each clip must have one materialized file" }
        val command = mutableListOf("ffmpeg", "-y")
        clips.zip(files).forEach { (clip, file) ->
            if (clip.asset.mimeType.startsWith("image/")) {
                command.addAll(listOf("-loop", "1", "-t", (clip.durationMs / 1000.0).toString(), "-i", file.absolutePath))
            } else {
                command.addAll(listOf("-i", file.absolutePath))
            }
        }
        val filter = clips.indices.joinToString(";") { i ->
            "[$i:v]setpts=PTS-STARTPTS[v$i]"
        } + ";" + clips.indices.joinToString("") { "[v$it]" } +
            "concat=n=${clips.size}:v=1:a=0[vout]"
        command.addAll(listOf("-filter_complex", filter, "-map", "[vout]", "-c:v", "libx264", "-pix_fmt", "yuv420p", "-movflags", "+faststart", output.absolutePath))
        return command
    }
}
