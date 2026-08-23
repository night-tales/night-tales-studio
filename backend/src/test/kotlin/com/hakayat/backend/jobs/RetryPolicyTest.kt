package com.hakayat.backend.jobs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RetryPolicyTest {
    private val policy = RetryPolicy(maxAttempts = 3, baseDelayMs = 1000)

    @Test fun `retry is bounded`() {
        assertTrue(policy.shouldRetry(0))
        assertTrue(policy.shouldRetry(2))
        assertFalse(policy.shouldRetry(3))
    }

    @Test fun `delay grows exponentially`() {
        assertEquals(1000, policy.delayMs(0))
        assertEquals(2000, policy.delayMs(1))
        assertEquals(4000, policy.delayMs(2))
    }
}
