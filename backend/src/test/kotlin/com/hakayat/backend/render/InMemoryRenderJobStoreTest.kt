package com.hakayat.backend.render

import kotlin.test.Test
import kotlin.test.assertEquals
import java.util.UUID

class InMemoryRenderJobStoreTest {
    @Test
    fun `stores and retrieves jobs by id and project`() = kotlinx.coroutines.test.runTest {
        val store = InMemoryRenderJobStore()
        val project = UUID.randomUUID()
        val first = RenderJob(UUID.randomUUID(), project)
        val second = RenderJob(UUID.randomUUID(), project)

        store.save(first)
        store.save(second)

        assertEquals(first, store.find(first.id))
        assertEquals(setOf(first.id, second.id), store.listByProject(project).map { it.id }.toSet())
    }
}