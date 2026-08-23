package com.hakayat.backend.media

import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.InputStream

interface ObjectStorage {
    suspend fun put(key: String, contentType: String, contentLength: Long, body: InputStream): String
    suspend fun delete(key: String)
}

class S3ObjectStorage(
    private val client: S3Client,
    private val bucket: String,
    private val publicBaseUrl: String? = null
) : ObjectStorage {
    override suspend fun put(key: String, contentType: String, contentLength: Long, body: InputStream): String {
        client.putObject(
            PutObjectRequest.builder().bucket(bucket).key(key).contentType(contentType).contentLength(contentLength).build(),
            RequestBody.fromInputStream(body, contentLength)
        )
        return publicBaseUrl?.trimEnd('/')?.let { "$it/$key" } ?: "s3://$bucket/$key"
    }

    override suspend fun delete(key: String) {
        client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build())
    }
}

class UnconfiguredObjectStorage : ObjectStorage {
    override suspend fun put(key: String, contentType: String, contentLength: Long, body: InputStream): String =
        error("Object storage is not configured")

    override suspend fun delete(key: String) = error("Object storage is not configured")
}
