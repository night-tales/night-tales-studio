package com.hakayat.backend.render

import java.util.UUID

interface RenderJobFailureHandler {
    suspend fun handle(failure: RenderJobFailure): Boolean
}

class RetryAwareRenderJobFailureHandler(
    private val policy: RenderJobRetryPolicy,
    private val queue: RenderJobQueue
) : RenderJobFailureHandler {
    override suspend fun handle(failure: RenderJobFailure): Boolean {
        if (!policy.shouldRetry(failure.attempt)) return false
        queue.enqueue(failure.jobId)
        return true
    }
}