package com.hakayat.backend.tasks

import com.hakayat.backend.ai.AiProvider
import com.hakayat.backend.ai.AiStreamEvent
import com.hakayat.backend.ai.TaskAiExecutor
import com.hakayat.backend.ai.UsageLedgerRepository
import com.hakayat.backend.ai.UsageRecord
import com.hakayat.backend.db.TaskRepository
import com.hakayat.backend.realtime.WebSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

class TaskWorker(
    private val scope: CoroutineScope,
    private val repository: TaskRepository,
    private val executor: TaskAiExecutor,
    private val usageLedger: UsageLedgerRepository,
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
            webSocketManager.sendProgressUpdate(task.userId, task.id.toString(), 0.1f, "جاري تنفيذ المهمة...")

            val startedAt = Instant.now()
            var response = ""
            var usage = com.hakayat.backend.ai.AiUsage()
            var providerRequestId: String? = null

            try {
                executeWithLease(task.id, repository) {
                    executor.stream(task.agentId, task.prompt, task.agentId).collect { event ->
                        when (event) {
                            is AiStreamEvent.Started -> {
                                providerRequestId = event.providerRequestId
                                repository.addEvent(task.id, "provider_started", """{"agentId":"${task.agentId}"}""")
                            }
                            is AiStreamEvent.TextDelta -> {
                                response += event.text
                                webSocketManager.sendTaskText(task.userId, task.id.toString(), event.text)
                            }
                            is AiStreamEvent.Completed -> {
                                response = event.response.text
                                usage = event.response.usage
                                providerRequestId = event.response.providerRequestId ?: providerRequestId
                            }
                            is AiStreamEvent.Failed -> throw event.error
                        }
                    }
                }

                repository.markCompleted(task.id, response)
                repository.addEvent(task.id, "completed", """{"progress":1,"inputTokens":${usage.inputTokens},"outputTokens":${usage.outputTokens}}""")

                val provider = when {
                    task.agentId.startsWith("gpt-") -> AiProvider.OPENAI
                    task.agentId.startsWith("claude-") -> AiProvider.ANTHROPIC
                    task.agentId.startsWith("gemini-") -> AiProvider.GEMINI
                    else -> throw IllegalArgumentException("Unsupported provider agent: ${task.agentId}")
                }

                usageLedger.record(
                    UsageRecord(
                        userId = task.userId,
                        taskId = task.id.toString(),
                        provider = provider,
                        model = task.agentId,
                        providerRequestId = providerRequestId,
                        inputTokens = usage.inputTokens,
                        outputTokens = usage.outputTokens,
                        latencyMs = Duration.between(startedAt, Instant.now()).toMillis(),
                        inputCost = null,
                        outputCost = null,
                        totalCost = null
                    )
                )

                repository.markCompleted(task.id, response)
                repository.addEvent(task.id, "completed", """{"progress":1,"inputTokens":\${usage.inputTokens},"outputTokens":\${usage.outputTokens}}""")
                webSocketManager.sendProgressUpdate(task.userId, task.id.toString(), 1f, "اكتملت المهمة")
            } catch (error: Exception) {
                val message = error.message ?: "Task execution failed"
                if (repository.retry(task.id, message)) {
                    repository.addEvent(task.id, "retry_scheduled")
                    webSocketManager.sendProgressUpdate(task.userId, task.id.toString(), 0.1f, "تعذر التنفيذ، ستتم إعادة المحاولة...")
                } else {
                    repository.markFailed(task.id, message)
                    repository.addEvent(task.id, "failed", """{"progress":0}""")
                    webSocketManager.sendProgressUpdate(task.userId, task.id.toString(), 0f, "فشلت المهمة")
                }
            }
        }
    }
}
