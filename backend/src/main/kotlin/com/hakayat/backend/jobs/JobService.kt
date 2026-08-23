package com.hakayat.backend.jobs

import com.hakayat.core.model.GenerationJob
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class JobService {
    private val jobs = ConcurrentHashMap<String, GenerationJob>()

    fun enqueue(projectId: String, type: String): GenerationJob {
        val job = GenerationJob(UUID.randomUUID().toString(), projectId, type, "queued")
        jobs[job.id] = job
        return job
    }

    fun get(id: String): GenerationJob? = jobs[id]
}
