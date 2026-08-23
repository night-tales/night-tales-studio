package com.hakayat.backend.infra

data class HealthReport(val status: String, val dependencies: List<DependencyHealth>)

class HealthService(private val checks: List<HealthCheck>) {
    suspend fun report(): HealthReport {
        val dependencies = checks.map { it.check() }
        val healthy = dependencies.all { it.status == "ok" || it.status == "configured" }
        return HealthReport(if (healthy) "ok" else "degraded", dependencies)
    }
}
