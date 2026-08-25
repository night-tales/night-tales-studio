package com.hakayat.backend.data

import com.hakayat.core.model.ProjectStatus
import com.hakayat.core.model.StoryProject
import java.util.UUID
import javax.sql.DataSource

data class ProjectRecord(
    val id: UUID,
    val title: String,
    val prompt: String,
    val status: String,
    val scenes: List<com.hakayat.core.model.Scene> = emptyList()
)

interface ProjectRepository {
    suspend fun findById(id: UUID): ProjectRecord?
    suspend fun save(project: ProjectRecord): ProjectRecord
}

class JdbcProjectRepository(private val dataSource: DataSource) : ProjectRepository {
    override suspend fun findById(id: UUID): ProjectRecord? = dataSource.connection.use { c ->
        c.prepareStatement("select id, title, prompt, status from projects where id = ?").use { s ->
            s.setObject(1, id)
            s.executeQuery().use { rs ->
                if (!rs.next()) null else ProjectRecord(
                    rs.getObject("id", UUID::class.java), rs.getString("title"),
                    rs.getString("prompt"), rs.getString("status")
                )
            }
        }
    }

    override suspend fun save(project: ProjectRecord): ProjectRecord = dataSource.connection.use { c ->
        c.prepareStatement(
            "insert into projects(id, title, prompt, status) values (?, ?, ?, ?) " +
                "on conflict (id) do update set title = excluded.title, prompt = excluded.prompt, status = excluded.status, updated_at = current_timestamp"
        ).use { s ->
            s.setObject(1, project.id); s.setString(2, project.title); s.setString(3, project.prompt); s.setString(4, project.status)
            s.executeUpdate()
        }
        project
    }
}

class InMemoryProjectRepository : ProjectRepository {
    private val projects = linkedMapOf<UUID, ProjectRecord>()
    override suspend fun findById(id: UUID): ProjectRecord? = projects[id]
    override suspend fun save(project: ProjectRecord): ProjectRecord { projects[project.id] = project; return project }
}

fun StoryProject.toRecord(): ProjectRecord = ProjectRecord(UUID.fromString(id), title, prompt, status.name.lowercase())
fun ProjectRecord.toDomain(): StoryProject = StoryProject(id.toString(), title, prompt, ProjectStatus.valueOf(status.uppercase()), scenes)
