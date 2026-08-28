package com.hakayat.backend.usage

data class UsageRecord(
    val taskId: String,
    val provider: String,
    val model: String,
    val inputTokens: Long = 0,
    val outputTokens: Long = 0,
    val costMicros: Long = 0,
)
