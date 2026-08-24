package com.hakayat.backend.jobs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class JobWorkerSupervisor(
    private val scope: CoroutineScope,
    private val queue: JobQueue,
    private val handler: suspend (QueuedGenerationJob) -> Unit,
    private val workers: Int = 2
) {
    fun start(): List<Job> = List(workers.coerceAtLeast(1)) {
        scope.launch { GenerationWorkerLoop(queue, handler).run() }
    }
}
