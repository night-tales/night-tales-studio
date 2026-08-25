package com.hakayat.backend.render

class RenderJobRetryPolicy(private val maxAttempts: Int = 3) {
    init { require(maxAttempts >= 1) }
    fun shouldRetry(attempt: Int): Boolean = attempt in 1 until maxAttempts
    fun nextAttempt(attempt: Int): Int = attempt + 1
}