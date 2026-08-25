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
import kotlinx.serialization.Serializable

class LlamaGenImageGenerationPort(
    private val apiKey: String,
    private val client: HttpClient,
    private val baseUrl: String = "https://api.llamagen.ai/v1",
    private val size: String = "1024x1024",
    private val fixPanelNum: Int = 1,
    private val pollDelayMs: Long = 3_000,
    private val maxPolls: Int = 100
) : ImageGenerationPort {

    init {
        require(apiKey.isNotBlank()) { "LLAMAGEN_API_KEY must not be blank" }
        require(fixPanelNum in 1..20) { "fixPanelNum must be between 1 and 20" }
        require(pollDelayMs > 0) { "pollDelayMs must be positive" }
        require(maxPolls > 0) { "maxPolls must be positive" }
    }

    override suspend fun generate(sceneId: String, prompt: String): String {
        require(prompt.isNotBlank()) { "image prompt must not be blank" }

        val created: LlamaGenGeneration = client.post("$baseUrl/comics/generations") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(CreateGenerationRequest(prompt = prompt, size = size, fixPanelNum = fixPanelNum))
        }.body()

        var latest = created
        repeat(maxPolls) {
            when (latest.status.uppercase()) {
                "FAILED", "CANCELLED" -> error(
                    "LlamaGen generation ${latest.id} failed with status ${latest.status}"
                )
                "PROCESSED" -> {
                    latest.firstAssetUrl()?.let { return it }
                    error("LlamaGen generation ${latest.id} is PROCESSED but contains no assetUrl")
                }
            }

            delay(pollDelayMs)
            latest = client.get("$baseUrl/comics/generations/${created.id}") {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
            }.body()
        }

        error("LlamaGen generation ${created.id} did not complete within ${maxPolls * pollDelayMs}ms")
    }

    private companion object {
        fun LlamaGenGeneration.firstAssetUrl(): String? =
            comics.asSequence()
                .flatMap { it.panels.asSequence() }
                .map { it.assetUrl }
                .firstOrNull(String::isNotBlank)
    }
}

@Serializable
private data class CreateGenerationRequest(
    val prompt: String,
    val size: String,
    val fixPanelNum: Int
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
    val assetUrl: String = "",
    val caption: String = ""
)
