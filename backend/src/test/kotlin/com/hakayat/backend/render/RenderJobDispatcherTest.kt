package com.hakayat.backend.render

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class RenderJobDispatcherTest {
    @Test
    fun `dispatch persists and enqueues job`() = runTest {
        val store = InMemoryRenderJobStore()
        val queue = InMemoryRenderJobQueue()
        val project = UUID.randomUUID()
        val job = RenderJobDispatcher(store, queue).dispatch(project)
        assertEquals(job.id, queue.dequeue())
        assertEquals(job, store.find(job.id))
    }
}