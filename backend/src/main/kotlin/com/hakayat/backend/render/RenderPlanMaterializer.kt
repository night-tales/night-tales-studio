package com.hakayat.backend.render

import java.io.File

data class MaterializedRenderPlan(
    val visual: List<File>,
    val audio: List<File>,
    val subtitles: List<File>
)

class RenderPlanMaterializer(private val materializer: AssetMaterializer) {
    suspend fun materialize(plan: RenderPlan, directory: File): MaterializedRenderPlan {
        directory.mkdirs()
        val typed = TypedAssetMaterializer(materializer)
        fun materializeAll(items: List<ResolvedTimelineAsset>): List<File> = items.map { asset ->
            typed.materialize(asset, directory)
        }
        return MaterializedRenderPlan(
            visual = materializeAll(plan.visual),
            audio = materializeAll(plan.audio),
            subtitles = materializeAll(plan.subtitles)
        )
    }
}