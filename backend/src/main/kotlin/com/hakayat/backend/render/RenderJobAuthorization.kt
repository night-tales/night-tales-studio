package com.hakayat.backend.render

import java.util.UUID

data class RenderPrincipal(val subject: String)

interface RenderProjectAuthorizer {
    suspend fun canRender(principal: RenderPrincipal, projectId: UUID): Boolean
}

class AuthorizedRenderJobService(
    private val authorizer: RenderProjectAuthorizer,
    private val jobs: RenderJobApiService
) {
    suspend fun create(principal: RenderPrincipal, projectId: UUID): RenderJobResponse {
        require(authorizer.canRender(principal, projectId)) { "render access denied" }
        return jobs.create(projectId)
    }
}