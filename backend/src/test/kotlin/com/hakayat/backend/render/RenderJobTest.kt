package com.hakayat.backend.render

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertFailsWith

class RenderJobTest {
    @Test
    fun `progress outside range is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            RenderJob(UUID.randomUUID(), UUID.randomUUID(), progress = 101)
        }
    }
}