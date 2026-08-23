package com.hakayat.backend.data

import com.hakayat.core.model.GenerationJob
import com.hakayat.core.model.StoryProject

interface ProjectRepository {
    suspend fun create(project: StoryProject): StoryProject
    suspend fun find(id: String): StoryProject?
}

interface GenerationJobRepository {
    suspend fun create(job: GenerationJob): GenerationJob
    suspend fun find(id: String): GenerationJob?
    suspend fun updateStatus(id: String, status: String): Boolean
}

class InMemoryProjectRepository : ProjectRepository {
    private val projects = linkedMapOf<String, StoryProject>()
    override suspend fun create(project: StoryProject): StoryProject { projects[project.id] = project; return project }
    override suspend fun find(id: String): StoryProject? = projects[id]
}

class InMemoryGenerationJobRepository : GenerationJobRepository {
    private val jobs = linkedMapOf<String, GenerationJob>()
    override suspend fun create(job: GenerationJob): GenerationJob { jobs[job.id] = job; return job }
    override suspend fun find(id: String): GenerationJob? = jobs[id]
    override suspend fun updateStatus(id: String, status: String): Boolean {
        val current = jobs[id] ?: return false
        jobs[id] = current.copy(status = status)
        return true
    }
}
