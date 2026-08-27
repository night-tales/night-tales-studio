package com.hakayat.backend.db

import java.util.UUID

data class TaskExecution(
    val id: UUID,
    val userId: String,
    val agentId: String,
    val prompt: String
)

data class TaskRecord(
    val id: UUID,
    val userId: String,
    val status: TaskStatus,
    val progress: Float,
    val error: String? = null,
    val output: String? = null
)

class TaskRepository(private val workerId: String = UUID.randomUUID().toString()) {
    fun create(userId: String, agentId: String, prompt: String, idempotencyKey: String? = null): TaskRecord =
        DatabaseFactory.transaction { connection ->
            val id = UUID.randomUUID()
            connection.prepareStatement("INSERT INTO users (id) VALUES (?) ON CONFLICT (id) DO NOTHING").use { statement ->
                statement.setString(1, userId)
                statement.executeUpdate()
            }
            connection.prepareStatement("INSERT INTO agents (id, name, provider, model) VALUES (?, ?, ?, ?) ON CONFLICT (id) DO NOTHING").use { statement ->
                statement.setString(1, agentId)
                statement.setString(2, agentId)
                statement.setString(3, when {
                    agentId.startsWith("gpt-") -> "openai"
                    agentId.startsWith("gemini-") -> "gemini"
                    agentId.startsWith("claude-") -> "anthropic"
                    else -> "unknown"
                })
                statement.setString(4, agentId)
                statement.executeUpdate()
            }
            connection.prepareStatement(
                """
                INSERT INTO tasks (id, user_id, agent_id, status, input, progress, idempotency_key)
                VALUES (?, ?, ?, 'QUEUED', jsonb_build_object('prompt', ?::text), 0, ?)
                """.trimIndent()
            ).use { statement ->
                statement.setObject(1, id)
                statement.setString(2, userId)
                statement.setString(3, agentId)
                statement.setString(4, prompt)
                statement.setString(5, idempotencyKey)
                statement.executeUpdate()
            }
            TaskRecord(id, userId, TaskStatus.QUEUED, 0f)
        }

    fun markRunning(id: UUID) = updateStatus(id, TaskStatus.RUNNING, 0.1f, null, null)

    fun claimNextQueued(): TaskExecution? = DatabaseFactory.transaction { connection ->
        connection.prepareStatement(
            """
            SELECT id, user_id, agent_id, input->>'prompt' AS prompt
            FROM tasks
            WHERE (status = 'QUEUED' AND (next_attempt_at IS NULL OR next_attempt_at <= NOW()))
               OR (status = 'RUNNING' AND lease_expires_at < NOW())
            ORDER BY created_at ASC
            FOR UPDATE SKIP LOCKED
            LIMIT 1
            """.trimIndent()
        ).use { statement ->
            statement.executeQuery().use { rs ->
                if (!rs.next()) return@transaction null
                val task = TaskExecution(
                    id = rs.getObject("id", UUID::class.java),
                    userId = rs.getString("user_id"),
                    agentId = rs.getString("agent_id"),
                    prompt = rs.getString("prompt")
                )
                connection.prepareStatement(
                    "UPDATE tasks SET status = 'RUNNING', progress = 0.1, started_at = COALESCE(started_at, NOW()), lease_owner = ?, lease_expires_at = NOW() + INTERVAL '60 seconds' WHERE id = ?"
                ).use { update ->
                    update.setString(1, workerId)
                    update.setObject(2, task.id)
                    update.executeUpdate()
                }
                task
            }
        }
    }

    fun renewLease(id: UUID): Boolean = DatabaseFactory.transaction { connection ->
        connection.prepareStatement("UPDATE tasks SET lease_expires_at = NOW() + INTERVAL '60 seconds' WHERE id = ? AND lease_owner = ? AND status = 'RUNNING'").use { statement ->
            statement.setObject(1, id)
            statement.setString(2, workerId)
            statement.executeUpdate() == 1
        }
    }

    fun markCompleted(id: UUID, output: String) =
        updateStatus(id, TaskStatus.COMPLETED, 1f, null, output)

    fun markFailed(id: UUID, error: String) =
        updateStatus(id, TaskStatus.FAILED, 0f, error, null)

    fun findOwned(id: UUID, userId: String): TaskRecord? =
        DatabaseFactory.transaction { connection ->
            connection.prepareStatement(
                """
                SELECT id, user_id, status, progress, error, output
                FROM tasks
                WHERE id = ? AND user_id = ?
                """.trimIndent()
            ).use { statement ->
                statement.setObject(1, id)
                statement.setString(2, userId)
                statement.executeQuery().use { rs ->
                    if (!rs.next()) return@transaction null
                    TaskRecord(
                        id = rs.getObject("id", UUID::class.java),
                        userId = rs.getString("user_id"),
                        status = TaskStatus.valueOf(rs.getString("status")),
                        progress = rs.getFloat("progress"),
                        error = rs.getString("error"),
                        output = rs.getString("output")
                    )
                }
            }
        }

    private fun updateStatus(
        id: UUID,
        status: TaskStatus,
        progress: Float,
        error: String?,
        output: String?
    ) {
        DatabaseFactory.transaction { connection ->
            val sql = when (status) {
                TaskStatus.RUNNING ->
                    "UPDATE tasks SET status = ?, progress = ?, started_at = NOW(), lease_owner = ?, lease_expires_at = NOW() + INTERVAL '60 seconds' WHERE id = ?"
                TaskStatus.COMPLETED ->
                    "UPDATE tasks SET status = ?, progress = ?, output = to_jsonb(?::text), completed_at = NOW(), lease_owner = NULL, lease_expires_at = NULL WHERE id = ?"
                TaskStatus.FAILED ->
                    "UPDATE tasks SET status = ?, progress = ?, error = ?, completed_at = NOW(), lease_owner = NULL, lease_expires_at = NULL WHERE id = ?"
                else ->
                    "UPDATE tasks SET status = ?, progress = ? WHERE id = ?"
            }

            connection.prepareStatement(sql).use { statement ->
                when (status) {
                    TaskStatus.COMPLETED -> {
                        statement.setString(1, status.name)
                        statement.setFloat(2, progress)
                        statement.setString(3, output ?: "")
                        statement.setObject(4, id)
                    }
                    TaskStatus.FAILED -> {
                        statement.setString(1, status.name)
                        statement.setFloat(2, progress)
                        statement.setString(3, error)
                        statement.setObject(4, id)
                    }
                    TaskStatus.RUNNING -> {
                        statement.setString(1, status.name)
                        statement.setFloat(2, progress)
                        statement.setString(3, workerId)
                        statement.setObject(4, id)
                    }
                    else -> {
                        statement.setString(1, status.name)
                        statement.setFloat(2, progress)
                        statement.setObject(3, id)
                    }
                }
                statement.executeUpdate()
            }
        }
    }
}
