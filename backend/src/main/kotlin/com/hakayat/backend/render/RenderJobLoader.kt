package com.hakayat.backend.render

import java.util.UUID

interface RenderJobLoader {
    suspend fun findById(id: UUID): RenderJob?
}