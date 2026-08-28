package com.hakayat.backend.tasks

import java.time.Instant

data class TaskLease(
    val taskId: String,
    val ownerId: String,
    val expiresAt: Instant,
)
