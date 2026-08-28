package com.hakayat.backend.ai

class AiAgentRegistry(
    adapters: Collection<AiProviderAdapter>
) {
    private val byProvider = adapters.associateBy { it.provider }

    fun get(agentId: String): AiProviderAdapter? {
        val normalized = agentId.lowercase()
        return when {
            normalized.startsWith("gpt-") || normalized.startsWith("openai:") -> byProvider[AiProvider.OPENAI]
            normalized.startsWith("claude-") || normalized.startsWith("anthropic:") -> byProvider[AiProvider.ANTHROPIC]
            normalized.startsWith("gemini-") || normalized.startsWith("gemini:") -> byProvider[AiProvider.GEMINI]
            else -> null
        }
    }

    fun require(agentId: String): AiProviderAdapter =
        get(agentId) ?: throw IllegalArgumentException("No provider configured for agent [$agentId]")
}
