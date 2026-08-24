package com.hakayat.backend.render

import java.util.UUID
import javax.sql.DataSource

interface TimelineRepository {
    suspend fun replace(timeline: Timeline)
    suspend fun find(projectId: UUID): Timeline?
}

class JdbcTimelineRepository(private val dataSource: DataSource) : TimelineRepository {
    override suspend fun replace(timeline: Timeline) = dataSource.connection.use { connection ->
        connection.autoCommit = false
        try {
            connection.prepareStatement("delete from timeline_clips where project_id = ?").use { statement ->
                statement.setObject(1, timeline.projectId)
                statement.executeUpdate()
            }
            connection.prepareStatement("insert into timeline_clips(id, project_id, asset_id, start_ms, duration_ms, track) values (?, ?, ?, ?, ?, ?)").use { statement ->
                timeline.clips.forEach { clip ->
                    statement.setObject(1, clip.id)
                    statement.setObject(2, timeline.projectId)
                    statement.setObject(3, clip.assetId)
                    statement.setLong(4, clip.startMs)
                    statement.setLong(5, clip.durationMs)
                    statement.setInt(6, clip.track)
                    statement.addBatch()
                }
                statement.executeBatch()
            }
            connection.commit()
        } catch (error: Throwable) {
            connection.rollback()
            throw error
        }
    }

    override suspend fun find(projectId: UUID): Timeline? = dataSource.connection.use { connection ->
        connection.prepareStatement("select id, asset_id, start_ms, duration_ms, track from timeline_clips where project_id = ? order by track, start_ms").use { statement ->
            statement.setObject(1, projectId)
            statement.executeQuery().use { rs ->
                val clips = buildList {
                    while (rs.next()) add(TimelineClip(rs.getObject("id", UUID::class.java), rs.getObject("asset_id", UUID::class.java), rs.getLong("start_ms"), rs.getLong("duration_ms"), rs.getInt("track")))
                }
                if (clips.isEmpty()) null else Timeline(projectId, clips)
            }
        }
    }
}
