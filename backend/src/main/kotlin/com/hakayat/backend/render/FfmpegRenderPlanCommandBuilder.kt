package com.hakayat.backend.render

class FfmpegRenderPlanCommandBuilder {
    fun build(plan: RenderPlan, output: String): List<String> {
        require(plan.visual.isNotEmpty()) { "Render plan requires visual assets" }
        val command = mutableListOf("ffmpeg", "-y")
        plan.visual.forEach { command.addAll(listOf("-i", it.asset.uri)) }
        plan.audio.firstOrNull()?.let { command.addAll(listOf("-i", it.asset.uri)) }
        plan.subtitles.firstOrNull()?.let { command.addAll(listOf("-i", it.asset.uri)) }
        command.addAll(listOf("-map", "0:v:0"))
        if (plan.audio.isNotEmpty()) command.addAll(listOf("-map", "${plan.visual.size}:a:0", "-c:a", "aac"))
        command.addAll(listOf("-c:v", "libx264", "-pix_fmt", "yuv420p"))
        if (plan.subtitles.isNotEmpty()) command.addAll(listOf("-vf", "subtitles=${plan.visual.size + plan.audio.size}.srt"))
        command.add(output)
        return command
    }
}