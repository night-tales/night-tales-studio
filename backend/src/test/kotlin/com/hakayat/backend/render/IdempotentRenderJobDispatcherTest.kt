package com.hakayat.backend.render

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import java.util.UUID

class IdempotentRenderJobDispatcherTest {
    @Test
    fun `reuses job for same project and key`() = runBlocking {
        val jobs = InMemoryRenderJobStore()
        val queue = InMemoryRenderJobQueue()
        val dispatcher = RenderJobDispatcher(jobs, queue)
        val idempotency = InMemoryRenderJobIdempotencyStore()
        val service = IdempotentRenderJobDispatcher(dispatcher, idempotency, jobs)
        val project = UUID.randomUUID()

        val first = service.dispatch(project, "request-1")
        val second = service.dispatch(project, "request-1")

        assertEquals(first.id, second.id)
        assertEquals(first, jobs.find(first.id))
        assertEquals(first.id, queue.dequeue())
        assertEquals(null, queue.dequeue())
    }
}