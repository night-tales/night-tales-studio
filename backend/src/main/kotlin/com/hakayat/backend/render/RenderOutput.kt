package com.hakayat.backend.render

import java.util.UUID

data class RenderOutput(val assetId: UUID, val uri: String)

interface RenderOutputStore {
    suspend fun store(jobId: UUID, projectId: UUID, renderedFile: java.io.File): RenderOutput
}