package com.hakayat.backend.render

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import java.util.UUID

fun Route.renderRoutes(api: RenderJobApiService) {
    post("/api/v1/projects/{projectId}/renders") {
        val projectId = call.parameters["projectId"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            ?: return@post call.respond(HttpStatusCode.BadRequest)
        call.respond(HttpStatusCode.Accepted, api.create(projectId))
    }
    get("/api/v1/projects/{projectId}/renders") {
        val projectId = call.parameters["projectId"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            ?: return@get call.respond(HttpStatusCode.BadRequest)
        call.respond(api.list(projectId))
    }
    get("/api/v1/renders/{jobId}") {
        val jobId = call.parameters["jobId"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            ?: return@get call.respond(HttpStatusCode.BadRequest)
        api.get(jobId)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound)
    }
}