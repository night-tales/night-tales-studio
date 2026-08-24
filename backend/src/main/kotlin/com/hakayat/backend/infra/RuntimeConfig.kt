package com.hakayat.backend.infra

data class RuntimeConfig(
    val port: Int,
    val databaseUrl: String?,
    val redisUrl: String?,
    val objectStorageEndpoint: String?,
    val aiProvider: String?
) {
    companion object {
        fun fromEnvironment(env: Map<String, String> = System.getenv()): RuntimeConfig = RuntimeConfig(
            port = env["PORT"]?.toIntOrNull() ?: 8080,
            databaseUrl = env["DATABASE_URL"],
            redisUrl = env["REDIS_URL"],
            objectStorageEndpoint = env["OBJECT_STORAGE_ENDPOINT"],
            aiProvider = env["AI_PROVIDER"]
        )
    }
}
