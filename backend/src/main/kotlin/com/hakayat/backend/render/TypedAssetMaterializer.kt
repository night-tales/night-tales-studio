package com.hakayat.backend.render

import java.io.File

class TypedAssetMaterializer(private val materializer: AssetMaterializer) {
    suspend fun materialize(asset: ResolvedTimelineAsset, directory: File): File {
        val extension = when {
            asset.asset.mimeType.startsWith("image/") -> "img"
            asset.asset.mimeType.startsWith("audio/") -> "audio"
            asset.asset.mimeType.startsWith("video/") -> "video"
            asset.asset.mimeType.startsWith("text/") -> "subtitle"
            else -> "asset"
        }
        return materializer.materialize(asset.asset.uri, File(directory, "${asset.asset.id}.$extension"))
    }
}
