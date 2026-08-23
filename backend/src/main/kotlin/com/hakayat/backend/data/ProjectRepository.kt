package com.hakayat.backend.data

import com.hakayat.core.model.StoryProject
import java.util.UUID

data class ProjectRecord(val id: UUID, val title: String, val status: String)

interface ProjectRepository {
    suspend fun findById(id: UUID): ProjectRecord?
    suspend fun save(project: ProjectRecord): ProjectRecord
}

class UnsupportedProjectRepository : ProjectRepository {
    override suspend fun findById(id: UUID): ProjectRecord? = null
    override suspend fun save(project: ProjectRecord): ProjectRecord =
        throw IllegalStateException("PostgreSQL repository is not configured")
}

/** Maps the domain project to persistence without exposing SQL details to the domain layer. */
fun StoryProject.toRecord(): ProjectRecord =
    ProjectRecord(UUID.fromString(id), title, status.name.lowercase())
