package com.hakayat.backend.ai

class AiAgentRegistry(
    adapters: Collection<AiProviderAdapter>
) {
    private val byId = adapters.associateBy { it.agentId }

    fun get(agentId: String): AiAgentAdapter? = byId[agentId]

    fun require(agentId: String): AiAgentAdapter =
        get(agentId) ?: throw IllegalArgumentException("Agent [$agentId] is not configured")
}
