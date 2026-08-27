package com.hakayat.backend.tasks

import com.hakayat.backend.Orchestrator
import com.hakayat.backend.db.TaskRepository
import com.hakayat.backend.realtime.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TaskWorker(
    private val scope: CoroutineScope,
    private val repository: TaskRepository,
    private val orchestrator: Orchestrator,
    private val webSocketManager: WebSocketManager
) {
    fun start(): Job = scope.launch(Dispatchers.IO) {
        while (isActive) {
            val task = runCatching { repository.claimNextQueued() }
                .onFailure { error -> println("Task worker database error: ${error.message}") }
                .getOrNull()

            if (task == null) {
                delay(500)
                continue
            }

            repository.addEvent(task.id, "started", """{"progress":0.1}""")
            webSocketManager.sendProgressUpdate(
                task.userId,
                task.id.toString(),
                0.1f,
                "جاري تنفيذ المهمة..."
            )

            try {
                val result = orchestrator.executeTask(task.prompt, task.agentId)
                repository.markCompleted(task.id, result)
                repository.addEvent(task.id, "completed", """{"progress":1}""")
                webSocketManager.sendProgressUpdate(
                    task.userId,
                    task.id.toString(),
                    1f,
                    "اكتملت المهمة"
                )
            } catch (error: Exception) {
                val message = error.message ?: "Task execution failed"
                if (repository.retry(task.id, message, 0)) {
                    repository.addEvent(task.id, "retry_scheduled")
                    webSocketManager.sendProgressUpdate(
                        task.userId,
                        task.id.toString(),
                        0.1f,
                        "تعذر التنفيذ، ستتم إعادة المحاولة..."
                    )
                } else {
                    repository.markFailed(task.id, message)
                    repository.addEvent(task.id, "failed", """{"progress":0}""")
                    webSocketManager.sendProgressUpdate(
                        task.userId,
                        task.id.toString(),
                        0f,
                        "فشلت المهمة"
                    )
                }
            }
        }
    }
}
