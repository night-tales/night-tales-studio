package com.hakayat.backend.render

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class SqlRenderJobRepositoryTest {
    @Test
    fun `save emits upsert parameters`() = runTest {
        var bind: Map<String, Any?> = emptyMap()
        val executor = object : RenderJobSqlExecutor {
            override suspend fun execute(sql: String, bind: Map<String, Any?>): List<Map<String, Any?>> {
                this@SqlRenderJobRepositoryTest
                return emptyList()
            }
        }
        val repository = SqlRenderJobRepository(executor)
        val job = RenderJob(UUID.randomUUID(), UUID.randomUUID(), attempt = 1)
        repository.save(job)
        assertEquals(1, job.attempt)
    }
}