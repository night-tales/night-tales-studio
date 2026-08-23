package com.hakayat.core.model

import kotlinx.serialization.Serializable

@Serializable
data class StoryProject(
    val id: String,
    val title: String,
    val prompt: String,
    val status: ProjectStatus = ProjectStatus.DRAFT,
    val scenes: List<Scene> = emptyList()
)

@Serializable
data class Scene(
    val id: String,
    val index: Int,
    val description: String,
    val imagePrompt: String? = null,
    val narrationText: String? = null,
    val durationMs: Long = 0
)

enum class ProjectStatus { DRAFT, PLANNING, GENERATING, READY, RENDERING, FAILED }

@Serializable
data class GenerationJob(val id: String, val projectId: String, val type: String, val status: String)
