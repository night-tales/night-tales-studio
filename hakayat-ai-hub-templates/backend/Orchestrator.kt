package com.hakayat.backend

import com.hakayat.backend.ai.AiAgentAdapter

class Orchestrator(private val agents: Map<String, AiAgentAdapter>) {
    
    // محرك التنسيق الأساسي (Orchestrator)
    suspend fun executeTask(prompt: String, agentId: String): String {
        println("Routing task to agent: ${agentId}")
        
        val agent = agents[agentId] 
            ?: return "عذراً، الوكيل [$agentId] غير مدعوم أو غير مفعل حالياً."

        println("Executing task using ${agent.agentId} adapter...")
        
        val result = agent.executeTask(prompt)
        
        return result.getOrElse { error ->
            "فشلت معالجة المهمة بواسطة [$agentId]: ${error.message}"
        }
    }
}
