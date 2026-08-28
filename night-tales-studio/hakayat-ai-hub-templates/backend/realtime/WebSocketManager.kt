package com.hakayat.backend.realtime

import io.ktor.websocket.*
import io.ktor.server.websocket.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

// مدير اتصالات WebSocket لإرسال الإشعارات والتقدم الحي للمستخدم
class WebSocketManager {
    // تخزين الاتصالات (المفاتيح هي معرفات الجلسات أو معرفات المستخدمين)
    private val connections = ConcurrentHashMap<String, MutableSet<DefaultWebSocketServerSession>>()

    fun addSession(userId: String, session: DefaultWebSocketServerSession) {
        connections.getOrPut(userId) { Collections.synchronizedSet(LinkedHashSet()) }.add(session)
    }

    fun removeSession(userId: String, session: DefaultWebSocketServerSession) {
        connections[userId]?.remove(session)
        if (connections[userId]?.isEmpty() == true) {
            connections.remove(userId)
        }
    }

    // إرسال تحديث تقدم إلى مستخدم معين
    suspend fun sendProgressUpdate(userId: String, taskId: String, progress: Float, message: String) {
        val jsonPayload = """{"type":"progress", "taskId":"$taskId", "progress":$progress, "message":"$message"}"""
        connections[userId]?.forEach { session ->
            try {
                session.send(Frame.Text(jsonPayload))
            } catch (e: Exception) {
                // تجاهل الاتصالات المغلقة، سيتم إزالتها لاحقاً
            }
        }
    }
}
