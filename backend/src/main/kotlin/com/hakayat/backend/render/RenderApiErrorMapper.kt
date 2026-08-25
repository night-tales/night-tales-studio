package com.hakayat.backend.render

import io.ktor.http.*

data class RenderApiError(val code: String, val message: String)

object RenderApiErrorMapper {
    fun map(error: Throwable): Pair<HttpStatusCode, RenderApiError> = when (error) {
        is IllegalArgumentException -> HttpStatusCode.BadRequest to RenderApiError("INVALID_REQUEST", error.message ?: "invalid request")
        else -> HttpStatusCode.InternalServerError to RenderApiError("RENDER_ERROR", "render operation failed")
    }
}