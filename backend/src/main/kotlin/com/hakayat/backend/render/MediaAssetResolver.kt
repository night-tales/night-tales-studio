package com.hakayat.backend.render

import com.hakayat.backend.media.MediaAssetRecord
import com.hakayat.backend.media.MediaAssetRepository
import java.util.UUID

class MediaAssetResolver(private val repository: MediaAssetRepository) {
    suspend fun resolveRecord(projectId: UUID, assetId: UUID): MediaAssetRecord {
        val asset = repository.findByProject(projectId).firstOrNull { it.id == assetId }
            ?: error("Media asset not found: $assetId")
        require(asset.status == "ready") { "Media asset is not ready: $assetId" }
        return asset
    }

    suspend fun resolve(projectId: UUID, assetId: UUID): String = resolveRecord(projectId, assetId).uri
}
