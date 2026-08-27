package com.hakayat.backend.db

import java.util.UUID

data class TaskRecord(
    val id: UUID,
    val userId: String,
    val status: TaskStatus,
    val progress: Float,
    val error: String? = null,
    val output: String? = null
)

class TaskRepository {
    fun create(userId: String, agentId: String, prompt: String): TaskRecord =
        DatabaseFactory.transaction { connection ->
            val id = UUID.randomUUID()
            connection.prepareStatement(
                """
                INSERT INTO tasks (id, user_id, agent_id, status, input, progress)
                VALUES (?, ?, ?, 'QUEUED', to_jsonb(?::text), 0)
                """.trimIndent()
            ).use { statement ->
                statement.setObject(1, id)
                statement.setString(2, userId)
                statement.setString(3, agentId)
                statement.setString(4, prompt)
                statement.executeUpdate()
            }
            TaskRecord(id, userId, TaskStatus.QUEUED, 0f)
        }

    fun markRunning(id: UUID) = updateStatus(id, TaskStatus.RUNNING, 0.1f, null, null)

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
                    "UPDATE tasks SET status = ?, progress = ?, started_at = NOW() WHERE id = ?"
                TaskStatus.COMPLETED ->
                    "UPDATE tasks SET status = ?, progress = ?, output = to_jsonb(?::text), completed_at = NOW() WHERE id = ?"
                TaskStatus.FAILED ->
                    "UPDATE tasks SET status = ?, progress = ?, error = ?, completed_at = NOW() WHERE id = ?"
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
