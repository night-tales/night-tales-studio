package com.hakayat.backend.data

import java.sql.Connection
import java.util.UUID
import javax.sql.DataSource

class JdbcProjectRepository(private val dataSource: DataSource) : ProjectRepository {
    override suspend fun findById(id: UUID): ProjectRecord? = dataSource.connection.use { connection ->
        connection.prepareStatement("select id, title, status from projects where id = ?").use { statement ->
            statement.setObject(1, id)
            statement.executeQuery().use { rs ->
                if (!rs.next()) null else ProjectRecord(
                    rs.getObject("id", UUID::class.java),
                    rs.getString("title"),
                    rs.getString("status")
                )
            }
        }
    }

    override suspend fun save(project: ProjectRecord): ProjectRecord = dataSource.connection.use { connection ->
        connection.prepareStatement(
            "insert into projects(id, title, status) values (?, ?, ?) on conflict (id) do update set title = excluded.title, status = excluded.status"
        ).use { statement ->
            statement.setObject(1, project.id)
            statement.setString(2, project.title)
            statement.setString(3, project.status)
            statement.executeUpdate()
        }
        project
    }
}
