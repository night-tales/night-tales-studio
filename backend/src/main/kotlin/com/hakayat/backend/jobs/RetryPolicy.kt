package com.hakayat.backend.jobs

data class RetryPolicy(val maxAttempts: Int = 3, val baseDelayMs: Long = 1_000L) {
    fun shouldRetry(attempt: Int): Boolean = attempt < maxAttempts
    fun delayMs(attempt: Int): Long = baseDelayMs * (1L shl attempt.coerceIn(0, 10))
}
