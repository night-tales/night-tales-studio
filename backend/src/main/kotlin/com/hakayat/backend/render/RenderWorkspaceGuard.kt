package com.hakayat.backend.render

class RenderWorkspaceGuard(private val manager: RenderWorkspaceManager) {
    suspend fun <T> withWorkspace(jobId: java.util.UUID, block: suspend (RenderWorkspace) -> T): T {
        val workspace = manager.create(jobId)
        return try { block(workspace) } finally { manager.cleanup(workspace) }
    }
}