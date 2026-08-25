package com.hakayat.backend.render

import java.util.UUID

class SqlRenderJobEventLog(private val sql: RenderJobSqlExecutor) : RenderJobEventLog {
    override suspend fun append(event: RenderJobEvent) {
        sql.execute("INSERT INTO render_job_events (job_id,status,progress,message,occurred_at) VALUES (:job_id,:status,:progress,:message,:occurred_at)", mapOf("job_id" to event.jobId, "status" to event.status.name, "progress" to event.progress, "message" to event.message, "occurred_at" to event.occurredAt))
    }
    override suspend fun list(jobId: UUID): List<RenderJobEvent> = sql.execute("SELECT job_id,status,progress,message,occurred_at FROM render_job_events WHERE job_id=:job_id ORDER BY occurred_at", mapOf("job_id" to jobId)).map { row -> RenderJobEvent(UUID.fromString(row["job_id"].toString()), RenderJobStatus.valueOf(row["status"].toString()), (row["progress"] as Number).toInt(), row["message"] as String?) }
}