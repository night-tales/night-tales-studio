package com.hakayat.backend.ai

import com.hakayat.core.model.Blueprint
import com.hakayat.core.model.Scene
import java.util.UUID

class PlannerAgent(private val llm: LlmProvider) {
    suspend fun plan(prompt: String): Blueprint = Blueprint(
        title = "Night Tales Project",
        logline = prompt,
        genre = "Fantasy",
        tone = "Cinematic",
        sceneCount = 6
    )
}

class StoryAgent(private val llm: LlmProvider) {
    suspend fun scenes(blueprint: Blueprint): List<Scene> = (1..blueprint.sceneCount).map {
        Scene(UUID.randomUUID().toString(), it, "Scene $it: ${blueprint.logline}", "cinematic scene $it", "Narration for scene $it", 5000)
    }
}

class OrchestratorAgent(private val planner: PlannerAgent, private val story: StoryAgent) {
    suspend fun generate(prompt: String): Pair<Blueprint, List<Scene>> {
        val blueprint = planner.plan(prompt)
        return blueprint to story.scenes(blueprint)
    }
}
