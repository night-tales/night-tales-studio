package com.hakayat.backend.render

data class RenderJobListQuery(val offset: Int = 0, val limit: Int = 20) {
    init {
        require(offset >= 0) { "offset must be non-negative" }
        require(limit in 1..100) { "limit must be between 1 and 100" }
    }
}