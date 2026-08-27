package com.hakayat.backend.tasks

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class LeaseRenewalTest {
    @Test
    fun executionReturnsResultWhenLeaseRemainsValid() = runBlocking {
        val result = executeWithLease(
            java.util.UUID.randomUUID(),
            repository = FakeTaskRepository()
        ) {
            delay(25)
            "ok"
        }
        assertEquals("ok", result)
    }
}

private class FakeTaskRepository : TaskLease {
    override fun renewLease(id: java.util.UUID): Boolean = true
}
