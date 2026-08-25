package com.hakayat.backend.jobs

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RedisJobQueueTest {
    @Test
    fun `enqueue serializes and dequeue restores generation job`() = runTest {
        val commands = RecordingRedisCommands()
        val queue = RedisJobQueue(commands)
        val job = QueuedGenerationJob(
            id = "job-1",
            projectId = "project-1",
            type = "story",
            attempt = 2
        )

        queue.enqueue(job)
        val restored = queue.dequeue()

        assertNotNull(restored)
        assertEquals(job, restored)
        assertEquals("night-tales:generation:jobs", commands.key)
    }

    private class RecordingRedisCommands : RedisCommands {
        var key = ""
        private var value: String? = null

        override suspend fun lpush(key: String, value: String) {
            this.key = key
            this.value = value
        }

        override suspend fun brpop(key: String, timeoutSeconds: Long): String? {
            this.key = key
            return value
        }
    }
}
