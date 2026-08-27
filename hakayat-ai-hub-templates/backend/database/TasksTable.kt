package com.hakayat.backend.database

import org.jetbrains.exposed.sql.Table

object TasksTable : Table("tasks") {
    val id = varchar("id", 80)
    val agentId = varchar("agent_id", 80)
    val prompt = text("prompt")
    val status = varchar("status", 30) // QUEUED, EXECUTING, COMPLETED, FAILED
    val result = text("result").nullable()
    val progress = float("progress").default(0f)
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(id)
}
