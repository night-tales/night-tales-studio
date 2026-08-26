package com.hakayat.backend.llamagen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LlamaGenCreateRequest(
    val prompt: String? = null,
    val promptUrl: String? = null,
    val model: String = "auto",
    val preset: String? = null,
    val size: String = "1024x1024",
    val fixPanelNum: Int? = null,
    val pagination: LlamaGenPagination? = null,
    val comicRoles: List<LlamaGenRole> = emptyList(),
    val comicLocations: List<LlamaGenLocation> = emptyList(),
    val characters: List<LlamaGenCharacter> = emptyList(),
    val attachments: List<LlamaGenAttachment> = emptyList(),
    val callbackUrl: String? = null,
    val webhookSecret: String? = null,
    val language: String? = null,
    val upscale: Boolean? = null
)

@Serializable
data class LlamaGenPagination(val totalPages: Int, val panelsPerPage: Int)

@Serializable
data class LlamaGenRole(
    val name: String,
    val age: Int? = null,
    val role: String? = null,
    val personality: String? = null,
    val catchphrase: String? = null,
    val dress: String? = null
)

@Serializable
data class LlamaGenLocation(val name: String, val description: String? = null)

@Serializable
data class LlamaGenCharacter(val name: String? = null, val url: String? = null, val assetId: String? = null)

@Serializable
data class LlamaGenAttachment(val type: String, val url: String? = null, val assetId: String? = null)

@Serializable
data class LlamaGenGenerationResponse(
    val id: String,
    val status: LlamaGenStatus,
    val data: LlamaGenData? = null,
    val comics: List<LlamaGenComic> = emptyList(),
    val error: LlamaGenError? = null
)

@Serializable
data class LlamaGenData(
    val assetUrl: String? = null,
    val panel: Int? = null,
    val caption: String? = null
)

@Serializable
data class LlamaGenComic(
    val page: Int? = null,
    val panel: Int? = null,
    val assetUrl: String? = null,
    val videoUrl: String? = null,
    val status: LlamaGenStatus? = null
)

@Serializable
data class LlamaGenError(val code: String? = null, val message: String? = null)

@Serializable
enum class LlamaGenStatus { QUEUED, PROCESSING, PROCESSED, FAILED, CANCELLED }
