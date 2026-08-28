package com.hakayat.backend.tasks

import kotlin.test.Test
import kotlin.test.assertEquals

class TaskLifecycleTest {
    @Test
    fun statusesIncludeTerminalAndRecoverableStates() {
        assertEquals(
            setOf("QUEUED", "RUNNING", "PAUSED", "COMPLETED", "FAILED", "CANCELLED"),
            com.hakayat.backend.db.TaskStatus.entries.map { it.name }.toSet()
        )
    }
}
