package com.hakayat.backend.render

data class RenderApiPage<T>(
    val items: List<T>,
    val nextCursor: String? = null,
    val total: Long? = null
)