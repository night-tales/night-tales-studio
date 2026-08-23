package com.hakayat.backend.media

import java.util.UUID
import javax.sql.DataSource

data class MediaAssetRecord(
    val id: UUID,
    val projectId: UUID,
    val sceneId: UUID?,
    val type: String,
    val uri: String,
    val mimeType: String,
    val status: String
)

interface MediaAssetRepository {
    suspend fun save(asset: MediaAssetRecord): MediaAssetRecord
    suspend fun findByProject(projectId: UUID): List<MediaAssetRecord>
}

class JdbcMediaAssetRepository(private val dataSource: DataSource) : MediaAssetRepository {
    override suspend fun save(asset: MediaAssetRecord): MediaAssetRecord = dataSource.connection.use { connection ->
        connection.prepareStatement(
            """
            insert into media_assets(id, project_id, scene_id, type, uri, mime_type, status)
            values (?, ?, ?, ?, ?, ?, ?)
            on conflict (id) do update set uri = excluded.uri, mime_type = excluded.mime_type, status = excluded.status
            """.trimIndent()
        ).use { statement ->
            statement.setObject(1, asset.id)
            statement.setObject(2, asset.projectId)
            statement.setObject(3, asset.sceneId)
            statement.setString(4, asset.type)
            statement.setString(5, asset.uri)
            statement.setString(6, asset.mimeType)
            statement.setString(7, asset.status)
            statement.executeUpdate()
        }
        asset
    }

    override suspend fun findByProject(projectId: UUID): List<MediaAssetRecord> = dataSource.connection.use { connection ->
        connection.prepareStatement(
            "select id, project_id, scene_id, type, uri, mime_type, status from media_assets where project_id = ? order by created_at"
        ).use { statement ->
            statement.setObject(1, projectId)
            statement.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) add(
                        MediaAssetRecord(
                            rs.getObject("id", UUID::class.java),
                            rs.getObject("project_id", UUID::class.java),
                            rs.getObject("scene_id", UUID::class.java),
                            rs.getString("type"),
                            rs.getString("uri"),
                            rs.getString("mime_type"),
                            rs.getString("status")
                        )
                    )
                }
            }
        }
    }
}
