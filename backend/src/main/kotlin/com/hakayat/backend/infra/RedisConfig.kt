package com.hakayat.backend.infra

import com.hakayat.backend.jobs.RedisCommands

/** Infrastructure factory boundary; concrete client stays outside domain and queue logic. */
object RedisConfig {
    fun commands(config: RuntimeConfig): RedisCommands? {
        // A concrete Redis client is supplied by the runtime composition layer.
        // Returning null keeps local development deterministic when REDIS_URL is absent.
        return if (config.redisUrl.isNullOrBlank()) null else null
    }
}
