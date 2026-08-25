package com.hakayat.backend.render

import java.util.UUID

interface RenderJobSqlExecutor {
    suspend fun execute(sql: String, bind: Map<String, Any?> = emptyMap()): List<Map<String, Any?>>
}

class SqlRenderJobRepository(private val sql: RenderJobSqlExecutor) : RenderJobRepository {
    override suspend fun save(job: RenderJob) {
        sql.execute(
            """
            INSERT INTO render_jobs (id, project_id, status, progress, attempt, output_asset_id, error)
            VALUES (:id, :project_id, :status, :progress, :attempt, :output_asset_id, :error)
            ON CONFLICT (id) DO UPDATE SET status=:status, progress=:progress, attempt=:attempt,
            output_asset_id=:output_asset_id, error=:error, updated_at=NOW()
            """.trimIndent(),
            mapOf(
                "id" to job.id,
                "project_id" to job.projectId,
                "status" to job.status.name,
                "progress" to job.progress,
                "attempt" to job.attempt,
                "output_asset_id" to job.outputAssetId,
                "error" to job.error
            )
        )
    }

    override suspend fun findById(id: UUID): RenderJob? =
        find("WHERE id=:id", mapOf("id" to id)).firstOrNull()

    override suspend fun findByProject(projectId: UUID): List<RenderJob> =
        find("WHERE project_id=:project_id ORDER BY created_at DESC", mapOf("project_id" to projectId))

    private suspend fun find(where: String, bind: Map<String, Any?>): List<RenderJob> =
        sql.execute(
            "SELECT id, project_id, status, progress, attempt, output_asset_id, error FROM render_jobs $where",
            bind
        ).map { row ->
            RenderJob(
                id = UUID.fromString(row["id"].toString()),
                projectId = UUID.fromString(row["project_id"].toString()),
                status = RenderJobStatus.valueOf(row["status"].toString()),
                progress = (row["progress"] as Number).toInt(),
                outputAssetId = row["output_asset_id"]?.let { UUID.fromString(it.toString()) },
                error = row["error"]?.toString(),
                attempt = (row["attempt"] as Number).toInt()
            )
        }
}