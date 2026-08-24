package com.hakayat.backend.render

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RenderJobServiceTest {
    @Test
    fun `creates queued job and persists it`() = kotlinx.coroutines.test.runTest {
        val store = InMemoryRenderJobStore()
        val projectId = UUID.randomUUID()
        val job = RenderJobService(store).create(projectId)
        assertEquals(projectId, job.projectId)
        assertEquals(RenderJobStatus.QUEUED, job.status)
        assertNotNull(store.find(job.id))
    }
}