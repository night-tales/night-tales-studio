package com.hakayat.backend.agents

import com.hakayat.core.model.Scene

interface LlmProvider {
    suspend fun generate(prompt: String): String
}

interface Agent<I, O> { suspend fun execute(input: I): O }

class StoryAgent(private val provider: LlmProvider) : Agent<String, String> {
    override suspend fun execute(input: String) = provider.generate("Write a coherent short story from: $input")
}

class PlannerAgent : Agent<String, List<Scene>> {
    override suspend fun execute(input: String): List<Scene> = listOf(
        Scene("scene-1", 1, input, imagePrompt = input, narrationText = input)
    )
}

class OrchestratorAgent(
    private val planner: PlannerAgent,
    private val story: StoryAgent
) {
    suspend fun run(prompt: String): List<Scene> {
        val storyText = story.execute(prompt)
        return planner.execute(storyText)
    }
}
