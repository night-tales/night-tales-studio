package com.hakayat.backend.render

import java.io.File

class FfmpegAudioMixerCommandBuilder {
    fun build(video: File, audioTracks: List<File>, output: File): List<String> {
        require(video.isFile) { "Video input does not exist" }
        require(audioTracks.isNotEmpty()) { "At least one audio track is required" }
        val command = mutableListOf("ffmpeg", "-y", "-i", video.absolutePath)
        audioTracks.forEach { track ->
            require(track.isFile) { "Audio input does not exist: ${track.absolutePath}" }
            command.addAll(listOf("-i", track.absolutePath))
        }
        val inputs = audioTracks.indices.joinToString("") { "[${it + 1}:a]" }
        command.addAll(listOf(
            "-filter_complex", "${inputs}amix=inputs=${audioTracks.size}:duration=longest:dropout_transition=2[aout]",
            "-map", "0:v:0", "-map", "[aout]", "-c:v", "copy", "-c:a", "aac", "-shortest", output.absolutePath
        ))
        return command
    }
}