package com.hakayat.backend.render

data class RenderJobFailureResult(val code: RenderJobErrorCode, val message: String, val retryable: Boolean)