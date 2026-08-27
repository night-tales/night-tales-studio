package com.hakayat.backend

import com.hakayat.backend.ai.adapters.*
import com.hakayat.backend.auth.FirebaseAuthConfig
import com.hakayat.backend.auth.FirebaseUserPrincipal
import com.hakayat.backend.db.DatabaseFactory
import com.hakayat.backend.db.TaskRepository
import com.hakayat.backend.realtime.WebSocketManager
import com.hakayat.backend.tasks.TaskWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.bearer.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.Serializable

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    FirebaseAuthConfig.initialize()
    DatabaseFactory.initialize()

    install(Authentication) {
        bearer("firebase") {
            authenticate { credentials ->
                try {
                    FirebaseAuthConfig.verifyIdToken(credentials.token)
                } catch (e: Exception) {
                    application.log.warn("Firebase authentication failed: ${e.javaClass.simpleName}")
                    null
                }
            }
        }
    }

    val webSocketManager = WebSocketManager()
    val taskRepository = TaskRepository()

    val openAiAdapter = OpenAiAdapter(
        System.getenv("OPENAI_API_KEY") ?: error("OPENAI_API_KEY must be configured")
    )
    val geminiAdapter = GeminiAdapter(
        System.getenv("GEMINI_API_KEY") ?: error("GEMINI_API_KEY must be configured")
    )
    val claudeAdapter = ClaudeAdapter(
        System.getenv("ANTHROPIC_API_KEY") ?: error("ANTHROPIC_API_KEY must be configured")
    )

    val agentsMap = mapOf(
        openAiAdapter.agentId to openAiAdapter,
        geminiAdapter.agentId to geminiAdapter,
        claudeAdapter.agentId to claudeAdapter
    )
    val orchestrator = Orchestrator(agentsMap)
    val workerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    TaskWorker(workerScope, taskRepository, orchestrator, webSocketManager).start()

    environment.monitor.subscribe(ApplicationStopping) {
        workerScope.coroutineContext.cancel()
    }

    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }

        authenticate("firebase") {
            get("/api/v1/auth/me") {
                val principal = call.principal<FirebaseUserPrincipal>()
                    ?: return@get call.respond(io.ktor.http.HttpStatusCode.Unauthorized)

                call.respond(mapOf(
                    "uid" to principal.uid,
                    "claims" to principal.claims
                ))
            }

            post("/api/v1/tasks") {
                val principal = call.principal<FirebaseUserPrincipal>()
                    ?: return@post call.respond(io.ktor.http.HttpStatusCode.Unauthorized)

                val request = call.receive<TaskRequest>()
                if (request.prompt.isBlank() || request.prompt.length > 20_000) {
                    return@post call.respond(
                        io.ktor.http.HttpStatusCode.BadRequest,
                        mapOf("error" to "prompt must contain 1-20000 characters")
                    )
                }
                if (request.agentId.isBlank()) {
                    return@post call.respond(
                        io.ktor.http.HttpStatusCode.BadRequest,
                        mapOf("error" to "agentId is required")
                    )
                }

                val idempotencyKey = request.idempotencyKey?.trim()?.takeIf { it.isNotEmpty() }
                if (idempotencyKey != null && idempotencyKey.length > 128) {
                    return@post call.respond(io.ktor.http.HttpStatusCode.BadRequest, mapOf("error" to "idempotencyKey too long"))
                }
                val existing = idempotencyKey?.let { taskRepository.findByIdempotencyKey(principal.uid, it) }
                if (existing != null) {
                    return@post call.respond(io.ktor.http.HttpStatusCode.Accepted, mapOf("status" to "existing", "taskId" to existing.id.toString()))
                }
                val task = try {
                    taskRepository.create(
                        userId = principal.uid,
                        agentId = request.agentId,
                        prompt = request.prompt,
                        idempotencyKey = idempotencyKey
                    )
                } catch (e: Exception) {
                    if (idempotencyKey != null) {
                        val raced = taskRepository.findByIdempotencyKey(principal.uid, idempotencyKey)
                        if (raced != null) return@post call.respond(io.ktor.http.HttpStatusCode.Accepted, mapOf("status" to "existing", "taskId" to raced.id.toString()))
                    }
                    throw e
                }

                call.respond(
                    io.ktor.http.HttpStatusCode.Accepted,
                    mapOf(
                        "status" to "queued",
                        "taskId" to task.id.toString()
                    )
                )
            }

            delete("/api/v1/tasks/{taskId}") {
                val principal = call.principal<FirebaseUserPrincipal>()
                    ?: return@delete call.respond(io.ktor.http.HttpStatusCode.Unauthorized)
                val taskId = call.parameters["taskId"]?.let { runCatching { java.util.UUID.fromString(it) }.getOrNull() }
                    ?: return@delete call.respond(io.ktor.http.HttpStatusCode.BadRequest)
                if (!taskRepository.cancelOwned(taskId, principal.uid)) {
                    return@delete call.respond(io.ktor.http.HttpStatusCode.Conflict, mapOf("error" to "Task is not cancellable"))
                }
                taskRepository.addEvent(taskId, "cancelled")
                call.respond(mapOf("status" to "cancelled", "taskId" to taskId.toString()))
            }

            get("/api/v1/tasks/{taskId}") {
                val principal = call.principal<FirebaseUserPrincipal>()
                    ?: return@get call.respond(io.ktor.http.HttpStatusCode.Unauthorized)

                val taskId = call.parameters["taskId"]?.let {
                    runCatching { java.util.UUID.fromString(it) }.getOrNull()
                } ?: return@get call.respond(
                    io.ktor.http.HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid taskId")
                )

                val task = taskRepository.findOwned(taskId, principal.uid)
                    ?: return@get call.respond(io.ktor.http.HttpStatusCode.NotFound)

                call.respond(task)
            }
        }
    }
}

@Serializable
data class TaskRequest(val prompt: String, val agentId: String, val idempotencyKey: String? = null)
