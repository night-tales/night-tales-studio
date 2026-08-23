package com.hakayat.backend.infra

import kotlin.test.Test
import kotlin.test.assertEquals

class RuntimeConfigTest {
    @Test
    fun `environment configuration is parsed without secrets`() {
        val config = RuntimeConfig.fromEnvironment(
            mapOf("PORT" to "9090", "DATABASE_URL" to "postgres://db", "REDIS_URL" to "redis://cache")
        )
        assertEquals(9090, config.port)
        assertEquals("postgres://db", config.databaseUrl)
        assertEquals("redis://cache", config.redisUrl)
    }
}
