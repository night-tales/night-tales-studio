package com.hakayat.backend.ai

interface LlmProvider {
    suspend fun generate(system: String, prompt: String): String
}

class MockLlmProvider : LlmProvider {
    override suspend fun generate(system: String, prompt: String): String = "{\"status\":\"ok\",\"prompt\":${jsonString(prompt)}}"

    private fun jsonString(value: String) = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}

class AiProviderRegistry(private val providers: Map<String, LlmProvider>) {
    fun get(name: String): LlmProvider = providers[name] ?: error("Unknown AI provider: $name")
}
