package com.hakayat.backend.infra

data class DependencyHealth(val name: String, val status: String, val detail: String? = null)

interface HealthCheck {
    suspend fun check(): DependencyHealth
}

class StaticHealthCheck(private val dependency: String) : HealthCheck {
    override suspend fun check() = DependencyHealth(dependency, "configured")
}
