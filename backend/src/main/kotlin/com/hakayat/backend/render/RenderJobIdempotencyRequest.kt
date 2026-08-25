package com.hakayat.backend.render

data class RenderJobIdempotencyRequest(
    val idempotencyKey: String
) {
    init { require(idempotencyKey.length in 1..128) }
}