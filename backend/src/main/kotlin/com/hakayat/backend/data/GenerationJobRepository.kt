package com.hakayat.backend.data

import java.util.UUID
import javax.sql.DataSource

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

class JdbcGenerationJobRepository(private val dataSource: DataSource) : GenerationJobRepository {
    override suspend fun findById(id: UUID): GenerationJobRecord? = dataSource.connection.use { c ->
        c.prepareStatement("select id, project_id, type, status, progress, error from generation_jobs where id = ?").use { s ->
            s.setObject(1, id)
            s.executeQuery().use { rs ->
                if (!rs.next()) null else GenerationJobRecord(
                    rs.getObject("id", UUID::class.java),
                    rs.getObject("project_id", UUID::class.java),
                    rs.getString("type"), rs.getString("status"),
                    rs.getInt("progress"), rs.getString("error")
                )
            }
        }
    }

    override suspend fun save(job: GenerationJobRecord): GenerationJobRecord = dataSource.connection.use { c ->
        c.prepareStatement("insert into generation_jobs(id, project_id, type, status, progress, error) values (?, ?, ?, ?, ?, ?) on conflict (id) do update set status = excluded.status, progress = excluded.progress, error = excluded.error").use { s ->
            s.setObject(1, job.id); s.setObject(2, job.projectId); s.setString(3, job.type)
            s.setString(4, job.status); s.setInt(5, job.progress); s.setString(6, job.error)
            s.executeUpdate()
        }
        job
    }

    override suspend fun updateStatus(id: UUID, status: String, progress: Int, error: String?) = dataSource.connection.use { c ->
        c.prepareStatement("update generation_jobs set status = ?, progress = ?, error = ?, updated_at = current_timestamp where id = ?").use { s ->
            s.setString(1, status); s.setInt(2, progress); s.setString(3, error); s.setObject(4, id)
            s.executeUpdate()
        }
    }
}
