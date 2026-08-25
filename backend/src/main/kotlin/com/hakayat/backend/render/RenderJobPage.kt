package com.hakayat.backend.render

data class RenderJobPage<T>(val items: List<T>, val offset: Int, val limit: Int, val hasMore: Boolean) {
    init { require(offset >= 0); require(limit in 1..100) }
}