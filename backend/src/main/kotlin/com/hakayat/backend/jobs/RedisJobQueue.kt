package com.hakayat.backend.jobs

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface RedisCommands : AutoCloseable {
    suspend fun ping(): Boolean
    suspend fun lpush(key: String, value: String)
    suspend fun brpop(key: String, timeoutSeconds: Long): String?

    override fun close() = Unit
}

class RedisJobQueue(
    private val redis: RedisCommands,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : JobQueue {
    private val key = "night-tales:generation:jobs"

    override suspend fun enqueue(job: QueuedGenerationJob) {
        redis.lpush(key, json.encodeToString(job))
    }

    override suspend fun dequeue(): QueuedGenerationJob? =
        redis.brpop(key, 30)?.let { json.decodeFromString<QueuedGenerationJob>(it) }
}
