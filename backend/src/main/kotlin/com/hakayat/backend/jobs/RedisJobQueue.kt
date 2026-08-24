package com.hakayat.backend.jobs

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface RedisCommands {
    suspend fun lpush(key: String, value: String)
    suspend fun brpop(key: String, timeoutSeconds: Long): String?
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
