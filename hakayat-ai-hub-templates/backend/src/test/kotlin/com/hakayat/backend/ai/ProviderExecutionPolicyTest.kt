package com.hakayat.backend.ai

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ProviderExecutionPolicyTest {
    @Test
    fun retries_retryable_failure() = runBlocking {
        var attempts = 0
        val result = executeWithPolicy(
            ProviderExecutionPolicy(timeoutMs = 1_000, maxAttempts = 3, initialBackoffMs = 1, maxBackoffMs = 2)
        ) {
            attempts++
            if (attempts < 3) error("temporary")
            "ok"
        }
        assertEquals("ok", result)
        assertEquals(3, attempts)
    }

    @Test
    fun does_not_retry_invalid_request() = runBlocking {
        var attempts = 0
        assertFailsWith<IllegalArgumentException> {
            executeWithPolicy(ProviderExecutionPolicy(maxAttempts = 3, initialBackoffMs = 1)) {
                attempts++
                throw IllegalArgumentException("bad request")
            }
        }
        assertEquals(1, attempts)
    }

    @Test
    fun timeout_is_not_retried_by_default() = runBlocking {
        var attempts = 0
        assertFailsWith<Exception> {
            executeWithPolicy(
                ProviderExecutionPolicy(timeoutMs = 10, maxAttempts = 3, initialBackoffMs = 1)
            ) {
                attempts++
                delay(100)
            }
        }
        assertEquals(1, attempts)
    }

    @Test
    fun cancellation_propagates_immediately() = runBlocking {
        var attempts = 0
        assertFailsWith<CancellationException> {
            executeWithPolicy(ProviderExecutionPolicy(maxAttempts = 3, initialBackoffMs = 1)) {
                attempts++
                throw CancellationException("cancelled")
            }
        }
        assertTrue(attempts == 1)
    }
}
