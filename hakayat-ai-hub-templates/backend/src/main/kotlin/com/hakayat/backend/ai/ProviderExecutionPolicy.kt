package com.hakayat.backend.ai

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

data class ProviderExecutionPolicy(
    val primary: AiProvider = AiProvider.OPENAI,
    val fallback: AiProvider? = null,
    val timeoutMs: Long = 30_000,
    val maxAttempts: Int = 1,
    val initialBackoffMs: Long = 100,
    val maxBackoffMs: Long = 2_000,
)

fun ProviderExecutionPolicy.providersInOrder(): List<AiProvider> = listOfNotNull(primary, fallback).distinct()

suspend fun <T> executeWithPolicy(policy: ProviderExecutionPolicy, block: suspend () -> T): T {
    require(policy.maxAttempts > 0)
    var attempt = 0
    var backoff = policy.initialBackoffMs.coerceAtLeast(0)
    while (true) {
        attempt++
        try {
            return withTimeout(policy.timeoutMs.coerceAtLeast(1)) { block() }
        } catch (e: CancellationException) {
            if (e is TimeoutCancellationException) throw e
            throw e
        } catch (e: IllegalArgumentException) { throw e
        } catch (e: Exception) {
            if (attempt >= policy.maxAttempts) throw e
            if (backoff > 0) delay(backoff)
            backoff = (backoff * 2).coerceAtMost(policy.maxBackoffMs.coerceAtLeast(backoff))
        }
    }
}
