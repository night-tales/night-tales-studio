package com.hakayat.backend.jobs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GenerationWorkerSupervisor(
    private val scope: CoroutineScope,
    private val workers: List<GenerationJobWorker>,
    private val idleDelayMs: Long = 250L
) {
    fun start(): List<Job> = workers.map { worker ->
        scope.launch {
            while (isActive) {
                if (!worker.runOnce()) delay(idleDelayMs)
            }
        }
    }
}
