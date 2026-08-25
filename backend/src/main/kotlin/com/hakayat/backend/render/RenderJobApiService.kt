package com.hakayat.backend.render

import java.util.UUID

class RenderJobApiService(
    private val dispatcher: RenderJobDispatcher,
    private val query: RenderJobQueryService
) {
    suspend fun create(projectId: UUID): RenderJobResponse =
        RenderJobResponse.from(dispatcher.dispatch(projectId))

    suspend fun get(jobId: UUID): RenderJobResponse? = query.get(jobId)

    suspend fun list(projectId: UUID): List<RenderJobResponse> = query.list(projectId)
}