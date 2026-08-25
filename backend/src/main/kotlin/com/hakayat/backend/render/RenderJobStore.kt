package com.hakayat.backend.render

interface RenderJobStore {
    suspend fun save(job: RenderJob)
}