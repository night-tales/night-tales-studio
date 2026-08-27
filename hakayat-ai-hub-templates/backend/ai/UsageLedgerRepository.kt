package com.hakayat.backend.ai

import com.hakayat.backend.db.DatabaseFactory

interface UsageLedgerRepository {
    fun record(record: UsageRecord)
}

class JdbcUsageLedgerRepository : UsageLedgerRepository {
    override fun record(record: UsageRecord) {
        val sql = """
            INSERT INTO usage_records (
                user_id, task_id, provider, model, input_tokens, output_tokens,
                latency_ms, estimated_cost, provider_request_id, currency,
                input_cost, output_cost, total_cost, metadata
            ) VALUES (?, NULLIF(?, '')::uuid, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '{}'::jsonb)
            ON CONFLICT (provider, provider_request_id)
            WHERE provider_request_id IS NOT NULL
            DO UPDATE SET
                input_tokens = EXCLUDED.input_tokens,
                output_tokens = EXCLUDED.output_tokens,
                latency_ms = EXCLUDED.latency_ms,
                input_cost = EXCLUDED.input_cost,
                output_cost = EXCLUDED.output_cost,
                total_cost = EXCLUDED.total_cost,
                estimated_cost = EXCLUDED.total_cost
        """.trimIndent()

        DatabaseFactory.transaction { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, record.userId)
                statement.setString(2, record.taskId ?: "")
                statement.setString(3, record.provider.name)
                statement.setString(4, record.model)
                statement.setLong(5, record.inputTokens)
                statement.setLong(6, record.outputTokens)
                if (record.latencyMs == null) statement.setNull(7, java.sql.Types.BIGINT) else statement.setLong(7, record.latencyMs)
                if (record.totalCost == null) statement.setNull(8, java.sql.Types.NUMERIC) else statement.setBigDecimal(8, record.totalCost)
                statement.setString(9, record.providerRequestId)
                statement.setString(10, record.currency)
                if (record.inputCost == null) statement.setNull(11, java.sql.Types.NUMERIC) else statement.setBigDecimal(11, record.inputCost)
                if (record.outputCost == null) statement.setNull(12, java.sql.Types.NUMERIC) else statement.setBigDecimal(12, record.outputCost)
                if (record.totalCost == null) statement.setNull(13, java.sql.Types.NUMERIC) else statement.setBigDecimal(13, record.totalCost)
                statement.executeUpdate()
            }
        }
    }
}
