package com.hakayat.backend.render

import com.hakayat.backend.media.MediaAssetRepository
import java.util.UUID

class MediaAssetResolver(private val repository: MediaAssetRepository) {
    suspend fun resolve(projectId: UUID, assetId: UUID): String {
        val asset = repository.findByProject(projectId).firstOrNull { it.id == assetId }
            ?: error("Media asset not found: $assetId")
        require(asset.status == "ready") { "Media asset is not ready: $assetId" }
        return asset.uri
    }
}
