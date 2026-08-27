package com.hakayat.backend

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.auth.*
import io.ktor.server.auth.bearer.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import com.hakayat.backend.ai.adapters.*
import com.hakayat.backend.realtime.WebSocketManager
import com.hakayat.backend.auth.FirebaseAuthConfig
import com.hakayat.backend.auth.FirebaseUserPrincipal
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    
    FirebaseAuthConfig.initialize()

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
    
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    
    val webSocketManager = WebSocketManager()
    
    // تهيئة الوكلاء
    val openAiAdapter = OpenAiAdapter(System.getenv("OPENAI_API_KEY")
        ?: error("OPENAI_API_KEY must be configured"))
    val geminiAdapter = GeminiAdapter(System.getenv("GEMINI_API_KEY")
        ?: error("GEMINI_API_KEY must be configured"))
    val claudeAdapter = ClaudeAdapter(System.getenv("ANTHROPIC_API_KEY")
        ?: error("ANTHROPIC_API_KEY must be configured"))
    
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
        
        post("/api/v1/auth/mock-login") {
            // محاكاة تسجيل الدخول لتوليد JWT (للأغراض التطويرية)
            val userId = "user-${java.util.UUID.randomUUID()}"
            val token = JwtConfig.generateToken(userId)
            call.respond(mapOf("token" to token, "userId" to userId))
        }
        
        authenticate("auth-jwt") {
            post("/api/v1/tasks") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
                
                val request = call.receive<TaskRequest>()
                
                // في التطبيق الفعلي، سيتم حفظ المهمة في قاعدة البيانات هنا قبل تنفيذها
                
                // تنبيه المستخدم عبر WebSockets بأن المهمة بدأت
                webSocketManager.sendProgressUpdate(userId, "taskId_123", 0.1f, "جاري تحضير الوكيل...")
                
                val result = orchestrator.executeTask(request.prompt, request.agentId)
                
                // تنبيه المستخدم بانتهاء المهمة
                webSocketManager.sendProgressUpdate(userId, "taskId_123", 1.0f, "اكتملت المهمة")
                
                call.respond(mapOf("status" to "success", "result" to result))
            }
        }
        
        // Realtime authentication will be added through a short-lived ticket flow
        // once task persistence is in place. Anonymous/query-string identities are rejected.
    }
}

@kotlinx.serialization.Serializable
data class TaskRequest(val prompt: String, val agentId: String)
