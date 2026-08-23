package com.hakayat.backend.data

import java.util.UUID

data class GenerationJobRecord(
    val id: UUID,
    val projectId: UUID,
    val type: String,
    val status: String,
    val progress: Int = 0,
    val error: String? = null
)

interface GenerationJobRepository {
    suspend fun findById(id: UUID): GenerationJobRecord?
    suspend fun save(job: GenerationJobRecord): GenerationJobRecord
    suspend fun updateStatus(id: UUID, status: String, progress: Int, error: String? = null)
}

class UnsupportedGenerationJobRepository : GenerationJobRepository {
    override suspend fun findById(id: UUID): GenerationJobRecord? = null
    override suspend fun save(job: GenerationJobRecord): GenerationJobRecord =
        throw IllegalStateException("PostgreSQL repository is not configured")
    override suspend fun updateStatus(id: UUID, status: String, progress: Int, error: String?) =
        throw IllegalStateException("PostgreSQL repository is not configured")
}
