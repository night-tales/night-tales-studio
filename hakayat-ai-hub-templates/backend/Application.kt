package com.hakayat.backend

import com.hakayat.backend.ai.adapters.*
import com.hakayat.backend.auth.FirebaseAuthConfig
import com.hakayat.backend.auth.FirebaseUserPrincipal
import com.hakayat.backend.db.DatabaseFactory
import com.hakayat.backend.db.TaskRepository
import com.hakayat.backend.realtime.WebSocketManager
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

                val task = taskRepository.create(
                    userId = principal.uid,
                    agentId = request.agentId,
                    prompt = request.prompt
                )
                taskRepository.markRunning(task.id)
                webSocketManager.sendProgressUpdate(
                    principal.uid,
                    task.id.toString(),
                    0.1f,
                    "جاري تحضير الوكيل..."
                )

                try {
                    val result = orchestrator.executeTask(request.prompt, request.agentId)
                    taskRepository.markCompleted(task.id, result.toString())
                    webSocketManager.sendProgressUpdate(
                        principal.uid,
                        task.id.toString(),
                        1f,
                        "اكتملت المهمة"
                    )

                    call.respond(mapOf(
                        "status" to "completed",
                        "taskId" to task.id.toString(),
                        "result" to result
                    ))
                } catch (e: Exception) {
                    taskRepository.markFailed(task.id, e.message ?: "Task execution failed")
                    webSocketManager.sendProgressUpdate(
                        principal.uid,
                        task.id.toString(),
                        0f,
                        "فشلت المهمة"
                    )
                    call.respond(
                        io.ktor.http.HttpStatusCode.InternalServerError,
                        mapOf(
                            "status" to "failed",
                            "taskId" to task.id.toString(),
                            "error" to "Task execution failed"
                        )
                    )
                }
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
data class TaskRequest(val prompt: String, val agentId: String)
