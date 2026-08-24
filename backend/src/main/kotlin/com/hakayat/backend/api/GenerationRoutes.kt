package com.hakayat.backend.api

import com.hakayat.backend.jobs.JobQueue
import com.hakayat.backend.jobs.QueuedGenerationJob
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CreateGenerationRequest(val type: String = "story")

fun Route.generationRoutes(queue: JobQueue) {
    route("/api/v1/projects/{projectId}/generation") {
        post {
            val projectId = call.parameters["projectId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val request = call.receive<CreateGenerationRequest>()
            val job = QueuedGenerationJob(UUID.randomUUID().toString(), projectId, request.type)
            queue.enqueue(job)
            call.respond(HttpStatusCode.Accepted, job)
        }
    }
}
