package com.hakayat.backend.render

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class RenderJobRepositoryContractTest {
    @Test
    fun `finds jobs by id and project`() = runTest {
        val store = object : InMemoryRenderJobStore(), RenderJobRepository {
            override suspend fun findById(id: UUID) = find(id)
            override suspend fun findByProject(projectId: UUID) = listOfNotNull(findById(id = jobsForTest.firstOrNull { it.projectId == projectId }?.id ?: return@findByProject null))
            private val jobsForTest = mutableListOf<RenderJob>()
            override suspend fun save(job: RenderJob) { super.save(job); jobsForTest.removeAll { it.id == job.id }; jobsForTest.add(job) }
        }
        val project = UUID.randomUUID()
        val job = RenderJob(UUID.randomUUID(), project)
        store.save(job)
        assertEquals(job, store.findById(job.id))
        assertEquals(1, store.findByProject(project).size)
    }
}