package com.hakayat.backend.render

import com.hakayat.backend.media.MediaAssetRecord
import com.hakayat.backend.media.MediaAssetRepository
import com.hakayat.backend.media.ObjectStorage
import java.io.File
import java.io.FileInputStream
import java.util.UUID

class RenderOutputService(
    private val renderEngine: RenderEngine,
    private val storage: ObjectStorage,
    private val assets: MediaAssetRepository
) {
    suspend fun renderAndStore(timeline: Timeline, outputKey: String): MediaAssetRecord {
        val rendered = renderEngine.render(timeline, outputKey)
        val outputFile = File(java.net.URI.create(rendered.outputUri))
        require(outputFile.isFile) { "Render output does not exist: ${rendered.outputUri}" }
        val storedUri = FileInputStream(outputFile).use { input ->
            storage.put(outputKey, "video/mp4", outputFile.length(), input)
        }
        return assets.save(
            MediaAssetRecord(
                id = UUID.randomUUID(),
                projectId = timeline.projectId,
                sceneId = null,
                type = "render",
                uri = storedUri,
                mimeType = "video/mp4",
                status = "ready"
            )
        )
    }
}
