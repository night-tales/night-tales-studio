package com.hakayat.backend.tasks

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

suspend fun <T> executeWithLease(
    taskId: java.util.UUID,
    repository: TaskLease,
    block: suspend () -> T
): T = coroutineScope {
    val renewal = launch {
        while (isActive) {
            delay(20_000)
            if (!repository.renewLease(taskId)) {
                this@coroutineScope.cancel()
                break
            }
        }
    }
    try {
        block()
    } finally {
        renewal.cancel()
    }
}


interface TaskLease {
    fun renewLease(id: java.util.UUID): Boolean
}
