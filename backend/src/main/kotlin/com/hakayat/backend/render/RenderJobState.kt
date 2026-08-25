package com.hakayat.backend.render

object RenderJobState {
    fun terminal(status: RenderJobStatus): Boolean = status == RenderJobStatus.SUCCEEDED || status == RenderJobStatus.FAILED
}
