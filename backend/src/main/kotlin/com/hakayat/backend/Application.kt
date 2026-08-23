package com.hakayat.backend

import com.hakayat.core.model.GenerationJob
import com.hakayat.core.model.StoryProject
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import java.util.UUID

fun Application.module() {
    install(ContentNegotiation) { json() }
    routing {
        get("/health") { call.respond(mapOf("status" to "ok")) }
        post("/api/v1/projects") {
            val input = call.receive<StoryProject>()
            call.respond(HttpStatusCode.Created, input.copy(id = UUID.randomUUID().toString()))
        }
        post("/api/v1/projects/{id}/jobs") {
            val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            call.respond(HttpStatusCode.Accepted, GenerationJob(UUID.randomUUID().toString(), id, "story", "queued"))
        }
    }
}

fun main() = embeddedServer(Netty, port = System.getenv("PORT")?.toIntOrNull() ?: 8080, module = Application::module).start(wait = true)
