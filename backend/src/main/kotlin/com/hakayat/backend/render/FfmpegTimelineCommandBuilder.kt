package com.hakayat.backend.render

/** Builds a deterministic FFmpeg filter graph for a timeline of video/image assets. */
class FfmpegTimelineCommandBuilder {
    fun build(inputs: List<String>, output: String, durationMs: Long): List<String> {
        require(inputs.isNotEmpty()) { "Timeline requires at least one input" }
        require(durationMs > 0) { "Timeline duration must be positive" }
        val filter = inputs.indices.joinToString("") { "[$it:v]setpts=PTS-STARTPTS[v$it];" } +
            inputs.indices.joinToString("") { "[v$it]" } +
            "concat=n=${inputs.size}:v=1:a=0[vout]"
        return buildList {
            add("ffmpeg"); add("-y")
            inputs.forEach { addAll(listOf("-i", it)) }
            addAll(listOf("-filter_complex", filter, "-map", "[vout]", "-t", (durationMs / 1000.0).toString(), "-c:v", "libx264", "-pix_fmt", "yuv420p", output))
        }
    }
}
