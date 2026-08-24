package com.hakayat.backend.render

import java.io.File
import java.util.UUID

class RenderWorkspace(private val root: File) {
    fun create(): File {
        val directory = File(root, UUID.randomUUID().toString())
        require(directory.mkdirs()) { "Unable to create render workspace: ${directory.absolutePath}" }
        return directory
    }

    fun cleanup(directory: File) {
        if (!directory.exists()) return
        directory.walkBottomUp().forEach { it.delete() }
    }
}