package com.hakayat.backend.render

enum class RenderJobTransition { QUEUE_TO_RUNNING, RUNNING_TO_SUCCEEDED, RUNNING_TO_FAILED, FAILED_TO_QUEUE }

object RenderJobTransitions {
    fun allowed(from: RenderJobStatus, to: RenderJobStatus): Boolean = when (from to to) {
        RenderJobStatus.QUEUED to RenderJobStatus.RUNNING -> true
        RenderJobStatus.RUNNING to RenderJobStatus.SUCCEEDED -> true
        RenderJobStatus.RUNNING to RenderJobStatus.FAILED -> true
        RenderJobStatus.FAILED to RenderJobStatus.QUEUED -> true
        else -> from == to
    }
}