package com.hakayat.backend.render

import java.time.Instant
import java.util.UUID

class SqlRenderJobLeaser(private val sql: RenderJobSqlExecutor) : RenderJobLeaser {
    override suspend fun acquire(jobId: UUID, workerId: String, until: Instant): RenderJobLease? {
        val rows = sql.execute(
            """
            INSERT INTO render_job_leases (job_id, worker_id, expires_at)
            VALUES (:job_id, :worker_id, :expires_at)
            ON CONFLICT (job_id) DO UPDATE
            SET worker_id=:worker_id, expires_at=:expires_at
            WHERE render_job_leases.expires_at <= NOW()
            RETURNING job_id, worker_id, expires_at
            """.trimIndent(),
            mapOf("job_id" to jobId, "worker_id" to workerId, "expires_at" to until)
        )
        return rows.firstOrNull()?.let { RenderJobLease(jobId, it["worker_id"].toString(), it["expires_at"] as Instant) }
    }

    override suspend fun release(lease: RenderJobLease) {
        sql.execute(
            "DELETE FROM render_job_leases WHERE job_id=:job_id AND worker_id=:worker_id",
            mapOf("job_id" to lease.jobId, "worker_id" to lease.workerId)
        )
    }
}