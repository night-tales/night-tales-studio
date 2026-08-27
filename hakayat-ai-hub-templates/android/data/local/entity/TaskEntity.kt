package com.hakayat.aihub.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val agentId: String,
    val prompt: String,
    val status: String, // PENDING, EXECUTING, COMPLETED, FAILED
    val result: String?,
    val progress: Float,
    val createdAt: Long,
    val updatedAt: Long
)
