package com.hakayat.backend.render

import com.hakayat.backend.media.ObjectStorage
import java.io.File

interface AssetMaterializer {
    suspend fun materialize(uri: String, destination: File): File
}

/** Local-file implementation for render workers; remote storage adapters can implement the same port. */
class LocalAssetMaterializer : AssetMaterializer {
    override suspend fun materialize(uri: String, destination: File): File {
        val source = File(java.net.URI.create(uri))
        require(source.isFile) { "Asset does not exist: $uri" }
        destination.parentFile?.mkdirs()
        source.inputStream().use { input -> destination.outputStream().use { output -> input.copyTo(output) } }
        return destination
    }
}
