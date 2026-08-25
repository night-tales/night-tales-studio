package com.hakayat.backend.render

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RenderJobEventTest {
    @Test
    fun `event rejects invalid progress`() {
        assertFailsWith<IllegalArgumentException> {
            RenderJobEvent(UUID.randomUUID(), RenderJobStatus.RUNNING, 101)
        }
    }

    @Test
    fun `publisher preserves event order`() = kotlinx.coroutines.test.runTest {
        val publisher = InMemoryRenderJobEventPublisher()
        val id = UUID.randomUUID()
        publisher.publish(RenderJobEvent(id, RenderJobStatus.RUNNING, 10))
        publisher.publish(RenderJobEvent(id, RenderJobStatus.SUCCEEDED, 100))
        assertEquals(listOf(10, 100), publisher.all().map { it.progress })
    }
}