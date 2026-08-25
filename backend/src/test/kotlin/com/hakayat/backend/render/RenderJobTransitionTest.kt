package com.hakayat.backend.render

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RenderJobTransitionTest {
    @Test
    fun `allows normal lifecycle transitions`() {
        assertTrue(RenderJobTransitions.allowed(RenderJobStatus.QUEUED, RenderJobStatus.RUNNING))
        assertTrue(RenderJobTransitions.allowed(RenderJobStatus.RUNNING, RenderJobStatus.SUCCEEDED))
        assertTrue(RenderJobTransitions.allowed(RenderJobStatus.RUNNING, RenderJobStatus.FAILED))
        assertTrue(RenderJobTransitions.allowed(RenderJobStatus.FAILED, RenderJobStatus.QUEUED))
    }

    @Test
    fun `rejects transitions out of terminal success`() {
        assertFalse(RenderJobTransitions.allowed(RenderJobStatus.SUCCEEDED, RenderJobStatus.RUNNING))
        assertFalse(RenderJobTransitions.allowed(RenderJobStatus.SUCCEEDED, RenderJobStatus.FAILED))
    }
}