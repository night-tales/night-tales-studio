package com.hakayat.backend.render

class FfmpegAudioVideoCommandBuilder {
    fun build(videoInputs: List<String>, audioInput: String?, subtitleInput: String?, output: String): List<String> {
        require(videoInputs.isNotEmpty()) { "At least one video input is required" }
        val command = mutableListOf("ffmpeg", "-y")
        videoInputs.forEach { command.addAll(listOf("-i", it)) }
        audioInput?.let { command.addAll(listOf("-i", it)) }
        subtitleInput?.let { command.addAll(listOf("-i", it)) }
        command.addAll(listOf("-map", "0:v:0"))
        if (audioInput != null) command.addAll(listOf("-map", "${videoInputs.size}:a:0"))
        command.addAll(listOf("-c:v", "libx264", "-pix_fmt", "yuv420p"))
        if (audioInput != null) command.addAll(listOf("-c:a", "aac", "-shortest"))
        if (subtitleInput != null) command.addAll(listOf("-vf", "subtitles=${subtitleInput}"))
        command.add(output)
        return command
    }
}
