package com.hakayat.backend.media

import java.io.InputStream

interface ObjectStorage {
    suspend fun put(key: String, contentType: String, input: InputStream): String
    suspend fun delete(key: String)
}

class UnconfiguredObjectStorage : ObjectStorage {
    override suspend fun put(key: String, contentType: String, input: InputStream): String =
        error("Object storage is not configured")

    override suspend fun delete(key: String) = error("Object storage is not configured")
}
