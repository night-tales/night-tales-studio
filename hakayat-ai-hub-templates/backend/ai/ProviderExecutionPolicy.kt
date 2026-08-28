package com.hakayat.backend.ai

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.math.min

data class ProviderExecutionPolicy(
    val timeoutMs: Long = 120_000,
    val maxAttempts: Int = 3,
    val initialBackoffMs: Long = 500,
    val maxBackoffMs: Long = 8_000
)

class ProviderExecutionTimeout(message: String) : RuntimeException(message)

fun interface ProviderRetryClassifier {
    fun isRetryable(error: Throwable): Boolean
}

object DefaultProviderRetryClassifier : ProviderRetryClassifier {
    override fun isRetryable(error: Throwable): Boolean =
        error !is CancellationException &&
            error !is IllegalArgumentException &&
            error !is ProviderExecutionTimeout
}

suspend fun <T> executeWithPolicy(
    policy: ProviderExecutionPolicy,
    classifier: ProviderRetryClassifier = DefaultProviderRetryClassifier,
    operation: suspend () -> T
): T {
    require(policy.maxAttempts >= 1)
    var attempt = 1
    var lastError: Throwable? = null

    while (attempt <= policy.maxAttempts) {
        try {
            return withTimeout(policy.timeoutMs) { operation() }
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            lastError = error
            if (attempt == policy.maxAttempts || !classifier.isRetryable(error)) throw error
            val backoff = min(policy.maxBackoffMs, policy.initialBackoffMs * (1L shl min(attempt - 1, 30)))
            delay(backoff)
            attempt++
        }
    }

    throw lastError ?: IllegalStateException("Provider execution failed without an error")
}
