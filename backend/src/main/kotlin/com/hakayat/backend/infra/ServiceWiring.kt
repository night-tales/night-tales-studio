package com.hakayat.backend.infra

import com.hakayat.backend.jobs.InMemoryJobQueue
import com.hakayat.backend.jobs.JobQueue
import com.hakayat.backend.jobs.RedisCommands
import com.hakayat.backend.jobs.RedisJobQueue

/** Explicit composition root: infrastructure choices stay outside domain services. */
object ServiceWiring {
    fun queue(redisCommands: RedisCommands?): JobQueue =
        redisCommands?.let { RedisJobQueue(it) } ?: InMemoryJobQueue()
}
