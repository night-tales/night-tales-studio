package com.hakayat.backend.data

/** SQL boundary for the production PostgreSQL adapter. */
interface SqlExecutor {
    suspend fun execute(sql: String, parameters: Map<String, Any?> = emptyMap()): Long
    suspend fun <T> query(sql: String, parameters: Map<String, Any?> = emptyMap(), mapper: (Map<String, Any?>) -> T): List<T>
}

class PostgresProjectRepository(private val sql: SqlExecutor) : ProjectRepository {
    override suspend fun create(project: com.hakayat.core.model.StoryProject): com.hakayat.core.model.StoryProject {
        sql.execute("insert into projects (id, title) values (:id, :title)", mapOf("id" to project.id, "title" to project.title))
        return project
    }

    override suspend fun find(id: String): com.hakayat.core.model.StoryProject? =
        sql.query("select id, title from projects where id = :id", mapOf("id" to id)) { row ->
            com.hakayat.core.model.StoryProject(
                id = row["id"].toString(),
                title = row["title"].toString()
            )
        }.firstOrNull()
}
