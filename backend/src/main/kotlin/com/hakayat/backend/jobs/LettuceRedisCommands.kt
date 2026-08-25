package com.hakayat.backend.jobs

import io.lettuce.core.RedisClient
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands

class LettuceRedisCommands(
    redisUrl: String
) : RedisCommands, AutoCloseable {
    private val client = RedisClient.create(redisUrl)
    private val connection = client.connect()
    private val commands: RedisCoroutinesCommands<String, String> = connection.coroutines()

    override suspend fun ping(): Boolean = runCatching {
        commands.ping() == "PONG"
    }.getOrDefault(false)

    override suspend fun lpush(key: String, value: String) {
        commands.lpush(key, value)
    }

    override suspend fun brpop(key: String, timeoutSeconds: Long): String? {
        val result = commands.brpop(timeoutSeconds, key)
        return result?.value
    }

    override fun close() {
        connection.close()
        client.shutdown()
    }
}
