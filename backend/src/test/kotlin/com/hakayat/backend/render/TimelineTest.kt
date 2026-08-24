package com.hakayat.backend.render

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TimelineTest {
    @Test fun `duration uses furthest clip end`() {
        val timeline = Timeline(UUID.randomUUID(), listOf(
            TimelineClip(UUID.randomUUID(), UUID.randomUUID(), 1000, 3000, 1),
            TimelineClip(UUID.randomUUID(), UUID.randomUUID(), 500, 1000, 0)
        ))
        assertEquals(4000, timeline.durationMs())
    }

    @Test fun `invalid duration is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            TimelineClip(UUID.randomUUID(), UUID.randomUUID(), 0, 0)
        }
    }
}
