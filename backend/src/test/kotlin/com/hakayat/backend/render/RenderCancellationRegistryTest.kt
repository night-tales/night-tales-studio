package com.hakayat.backend.render

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RenderCancellationRegistryTest {
    @Test
    fun `cancel clear lifecycle`() {
        val registry = InMemoryRenderCancellationRegistry()
        val id = UUID.randomUUID()
        assertFalse(registry.isCancelled(id))
        registry.cancel(id)
        assertTrue(registry.isCancelled(id))
        registry.clear(id)
        assertFalse(registry.isCancelled(id))
    }
}