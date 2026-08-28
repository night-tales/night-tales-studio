package com.hakayat.backend.ai

interface AiAgentAdapter {
    /**
     * مُعرف الوكيل (مثلاً: "openai-gpt4o", "gemini-1.5-pro", "claude-3.5-sonnet")
     */
    val agentId: String

    /**
     * تنفيذ المهمة بناءً على الطلب (Prompt) وإرجاع النتيجة
     */
    suspend fun executeTask(prompt: String): Result<String>
}
