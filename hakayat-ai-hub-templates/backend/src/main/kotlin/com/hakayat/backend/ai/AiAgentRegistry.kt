package com.hakayat.backend.ai

class AiAgentRegistry(adapters: List<AiAgentAdapter>) {
    private val adaptersByProvider = adapters.associateBy { it.provider }

    fun get(provider: AiProvider): AiAgentAdapter =
        adaptersByProvider[provider] ?: error("No AI adapter registered for $provider")
}
