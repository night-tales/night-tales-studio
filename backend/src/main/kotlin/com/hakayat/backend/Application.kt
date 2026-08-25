package com.hakayat.backend

import com.hakayat.backend.data.GenerationJobRepository
import com.hakayat.backend.data.InMemoryGenerationJobRepository
import com.hakayat.backend.data.InMemoryProjectRepository
import com.hakayat.backend.data.ProjectRepository
import com.hakayat.backend.data.ProjectRecord
import com.hakayat.backend.data.toRecord
import com.hakayat.backend.infra.HealthService
import com.hakayat.backend.infra.RedisConfig
import com.hakayat.backend.infra.RuntimeConfig
import com.hakayat.backend.infra.ServiceWiring
import com.hakayat.backend.infra.StaticHealthCheck
import com.hakayat.backend.jobs.GenerationJobWorker
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

    val projects: ProjectRepository = InMemoryProjectRepository()
    val jobs: GenerationJobRepository = InMemoryGenerationJobRepository()
    val redisCommands = RedisConfig.commands(config)
    val queue: JobQueue = ServiceWiring.queue(redisCommands)
    val health = HealthService(
        listOf(
            StaticHealthCheck("api"),
            StaticHealthCheck("postgres-config"),
            StaticHealthCheck("redis-config")
        )
    )

    environment.monitor.subscribe(ApplicationStopped) {
        redisCommands?.close()
    }

    val worker = GenerationJobWorker(queue, jobs) { job ->
        jobs.updateStatus(UUID.fromString(job.id), "completed", 100, attempt = job.attempt)
    }

    launch { GenerationWorkerLoop(worker).run() }

    routing {
        get("/health") { call.respond(health.report()) }
        get("/api/v1/config") {
            call.respond(mapOf("port" to config.port, "aiProvider" to config.aiProvider))
        }
        post("/api/v1/projects") {
            val input = call.receive<StoryProject>()
            val project = input.copy(id = UUID.randomUUID().toString())
            projects.save(project.toRecord())
            call.respond(HttpStatusCode.Created, project)
        }
        post("/api/v1/projects/{id}/jobs") {
            val projectId = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val projectUuid = projectId.toUuidOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)
            if (projects.findById(projectUuid) == null) return@post call.respond(HttpStatusCode.NotFound)

            val job = QueuedGenerationJob(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                type = "story"
            )
            jobs.save(
                com.hakayat.backend.data.GenerationJobRecord(
                    id = UUID.fromString(job.id),
                    projectId = projectUuid,
                    type = job.type,
                    status = "queued",
                    attempt = job.attempt
                )
            )
            queue.enqueue(job)
            call.respond(HttpStatusCode.Accepted, GenerationJob(job.id, job.projectId, job.type, "queued"))
        }
        get("/api/v1/jobs/{id}") {
            val id = call.parameters["id"]?.toUuidOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)
            jobs.findById(id)?.let {
                call.respond(GenerationJob(it.id.toString(), it.projectId.toString(), it.type, it.status))
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    }
}

private fun String.toUuidOrNull(): UUID? = runCatching { UUID.fromString(this) }.getOrNull()

fun main() = embeddedServer(Netty, port = RuntimeConfig.fromEnvironment().port, module = Application::module).start(wait = true)
