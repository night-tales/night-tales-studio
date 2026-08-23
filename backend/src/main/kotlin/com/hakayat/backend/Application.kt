package com.hakayat.backend

import com.hakayat.backend.data.InMemoryGenerationJobRepository
import com.hakayat.backend.data.InMemoryProjectRepository
import com.hakayat.backend.infra.HealthService
import com.hakayat.backend.infra.RuntimeConfig
import com.hakayat.backend.infra.StaticHealthCheck
import com.hakayat.backend.infra.ServiceWiring
import com.hakayat.backend.jobs.GenerationWorkerLoop
import com.hakayat.backend.jobs.JobQueue
import com.hakayat.backend.jobs.QueuedGenerationJob
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
import kotlinx.coroutines.launch
import java.util.UUID

fun Application.module() {
    val config = RuntimeConfig.fromEnvironment()
    install(ContentNegotiation) { json() }

    val projects = InMemoryProjectRepository()
    val jobs = InMemoryGenerationJobRepository()
    val queue: JobQueue = ServiceWiring.queue(null)
    val health = HealthService(
        listOf(
            StaticHealthCheck("api"),
            StaticHealthCheck("postgres-config"),
            StaticHealthCheck("redis-config")
        )
    )

    launch {
        GenerationWorkerLoop(queue) { job ->
            jobs.updateStatus(job.id, "running")
            jobs.updateStatus(job.id, "completed")
        }.run()
    }

    routing {
        get("/health") { call.respond(health.report()) }
        get("/api/v1/config") {
            call.respond(mapOf("port" to config.port, "aiProvider" to config.aiProvider))
        }
        post("/api/v1/projects") {
            val input = call.receive<StoryProject>()
            val project = input.copy(id = UUID.randomUUID().toString())
            projects.create(project)
            call.respond(HttpStatusCode.Created, project)
        }
        post("/api/v1/projects/{id}/jobs") {
            val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            if (projects.find(id) == null) return@post call.respond(HttpStatusCode.NotFound)
            val job = GenerationJob(UUID.randomUUID().toString(), id, "story", "queued")
            jobs.create(job)
            queue.enqueue(QueuedGenerationJob(job.id, id, job.type))
            call.respond(HttpStatusCode.Accepted, job)
        }
        get("/api/v1/jobs/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            jobs.find(id)?.let { call.respond(it) } ?: call.respond(HttpStatusCode.NotFound)
        }
    }
}

fun main() = embeddedServer(Netty, port = RuntimeConfig.fromEnvironment().port, module = Application::module).start(wait = true)
