package com.hakayat.backend.render

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import java.util.UUID

fun Route.renderApiRoutes(service: RenderJobApiService) {
    post("/api/v1/projects/{projectId}/renders") {
        val projectId = RenderApiValidation.uuid(call.parameters["projectId"], "projectId")
        val key = RenderApiValidation.idempotencyKey(call.request.header("Idempotency-Key"))
        call.respond(HttpStatusCode.Accepted, service.create(projectId, key))
    }
    get("/api/v1/renders/{jobId}") {
        val id = RenderApiValidation.uuid(call.parameters["jobId"], "jobId")
        service.get(id)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound)
    }
    get("/api/v1/projects/{projectId}/renders") {
        val projectId = RenderApiValidation.uuid(call.parameters["projectId"], "projectId")
        call.respond(service.list(projectId))
    }
}