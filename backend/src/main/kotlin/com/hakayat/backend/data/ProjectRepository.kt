package com.hakayat.backend.data

import java.util.UUID

data class ProjectRecord(val id: UUID, val title: String, val status: String)

interface ProjectRepository {
    suspend fun findById(id: UUID): ProjectRecord?
    suspend fun save(project: ProjectRecord): ProjectRecord
}

/** Infrastructure-neutral repository contract; SQL implementation is composed at runtime. */
class UnsupportedProjectRepository : ProjectRepository {
    override suspend fun findById(id: UUID): ProjectRecord? = null
    override suspend fun save(project: ProjectRecord): ProjectRecord =
        throw IllegalStateException("PostgreSQL repository is not configured")
}
