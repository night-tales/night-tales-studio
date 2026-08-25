package com.hakayat.backend.infra

import com.hakayat.backend.jobs.LettuceRedisCommands
import com.hakayat.backend.jobs.RedisCommands

/** Infrastructure factory boundary; concrete client stays outside domain and queue logic. */
object RedisConfig {
    fun commands(config: RuntimeConfig): RedisCommands? =
        config.redisUrl
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?.let(::LettuceRedisCommands)
}
