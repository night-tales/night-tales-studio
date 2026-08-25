package com.hakayat.backend.ai

import com.hakayat.backend.jobs.ImageGenerationPort
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class LlamaGenImageGenerationPort(
    private val apiKey: String,
    private val client: HttpClient,
    private val baseUrl: String = "https://api.llamagen.ai/v1",
    private val model: String = "cyani-model",
    private val size: String = "1024x1024",
    private val pollDelayMs: Long = 2_000,
    private val maxPolls: Int = 60
) : ImageGenerationPort {

    init {
        require(apiKey.isNotBlank()) { "LLAMAGEN_API_KEY must not be blank" }
    }

    override suspend fun generate(sceneId: String, prompt: String): String {
        require(prompt.isNotBlank()) { "image prompt must not be blank" }

        val created: LlamaGenGeneration = client.post("$baseUrl/comics/generations") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(CreateGenerationRequest(model = model, prompt = prompt, size = size))
        }.body()

        var latest = created
        repeat(maxPolls) {
            if (latest.status.uppercase() in TERMINAL_FAILURES) {
                error("LlamaGen generation ${latest.id} failed with status ${latest.status}")
            }
            latest.comics.firstOrNull()?.panels
                ?.firstOrNull { it.assetUrl.isNotBlank() }
                ?.assetUrl
                ?.let { return it }

            delay(pollDelayMs)
            latest = client.get("$baseUrl/comics/generations/${created.id}") {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
            }.body()
        }

        error("LlamaGen generation ${created.id} did not complete within ${maxPolls * pollDelayMs}ms")
    }

    private companion object {
        val TERMINAL_FAILURES = setOf("FAILED", "ERROR", "CANCELLED")
    }
}

@Serializable
private data class CreateGenerationRequest(
    val model: String,
    val prompt: String,
    val size: String
)

@Serializable
private data class LlamaGenGeneration(
    val id: String,
    val status: String,
    val comics: List<LlamaGenPage> = emptyList()
)

@Serializable
private data class LlamaGenPage(
    val page: Int = 0,
    val panels: List<LlamaGenPanel> = emptyList()
)

@Serializable
private data class LlamaGenPanel(
    val panel: Int = 0,
    @SerialName("assetUrl") val assetUrl: String = "",
    val caption: String = ""
)
