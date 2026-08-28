package com.hakayat.backend.ai.adapters

import com.hakayat.backend.ai.AiAgentAdapter
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*

class GeminiAdapter(private val apiKey: String) : AiAgentAdapter {
    override val agentId: String = "gemini-1.5-pro"

    // نهيئ Ktor HttpClient للاتصال بـ Google Gemini API
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    override suspend fun executeTask(prompt: String): Result<String> {
        return try {
            println("Sending request to Google Gemini API (gemini-1.5-pro)...")
            
            // في حال عدم وجود مفتاح حقيقي أثناء التطوير، نستخدم الرد الوهمي
            if (apiKey == "dummy-key" || apiKey.isBlank()) {
                return Result.success("هذا رد مُحاكى من Gemini 1.5 Pro بخصوص: '$prompt'")
            }

            val response: HttpResponse = client.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent") {
                parameter("key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("contents", buildJsonArray {
                            add(buildJsonObject {
                                put("parts", buildJsonArray {
                                    add(buildJsonObject { put("text", prompt) })
                                })
                            })
                        })
                    }
                )
            }

            if (response.status.isSuccess()) {
                val jsonResponse = response.body<JsonObject>()
                val generatedText = jsonResponse["candidates"]
                    ?.jsonArray?.get(0)
                    ?.jsonObject?.get("content")
                    ?.jsonObject?.get("parts")
                    ?.jsonArray?.get(0)
                    ?.jsonObject?.get("text")
                    ?.jsonPrimitive?.content ?: "لم يتم العثور على نص في الرد."
                
                Result.success(generatedText)
            } else {
                Result.failure(Exception("Gemini API Error: ${response.status} - ${response.bodyAsText()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
