package com.hakayat.backend.render

import java.io.File
import java.util.UUID

class RenderWorkspaceManager(private val root: File) {
    fun create(jobId: UUID): RenderWorkspace {
        val directory = File(root, jobId.toString()).apply { mkdirs() }
        require(directory.isDirectory) { "Unable to create render workspace" }
        return RenderWorkspace(directory)
    }

    fun cleanup(workspace: RenderWorkspace) {
        workspace.directory.deleteRecursively()
    }
}

data class RenderWorkspace(val directory: File)