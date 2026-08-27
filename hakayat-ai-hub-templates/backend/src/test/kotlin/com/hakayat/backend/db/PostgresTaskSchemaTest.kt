package com.hakayat.backend.db

import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PostgresTaskSchemaTest {
    @Test
    fun schemaSupportsIdempotencyAndWorkerLeases() {
        PostgreSQLContainer<Nothing>("postgres:16-alpine").use { postgres ->
            postgres.start()
            postgres.createConnection("").use { connection ->
                val schema = Files.readString(
                    java.nio.file.Paths.get("db/V1__foundation.sql")
                )
                connection.createStatement().use { statement ->
                    statement.execute(schema)
                }

                connection.createStatement().use { statement ->
                    statement.execute(
                    """
                    INSERT INTO users (id) VALUES ('integration-user');
                    INSERT INTO agents (id, name, provider, model)
                    VALUES ('gpt-test', 'GPT Test', 'openai', 'gpt-test');
                    INSERT INTO tasks (
                        user_id, agent_id, status, input, idempotency_key,
                        max_retries, lease_owner, lease_expires_at
                    ) VALUES (
                        'integration-user', 'gpt-test', 'QUEUED',
                        '{"prompt":"test"}', 'integration-key',
                        3, 'worker-a', NOW() + INTERVAL '60 seconds'
                    )
                    """.trimIndent()
                    )
                }

                connection.createStatement().use { statement ->
                    statement.executeQuery(
                        "SELECT max_retries, idempotency_key, lease_owner FROM tasks WHERE user_id = 'integration-user'"
                    ).use { rs ->
                        assertEquals(true, rs.next())
                        assertEquals(3, rs.getInt("max_retries"))
                        assertEquals("integration-key", rs.getString("idempotency_key"))
                        assertEquals("worker-a", rs.getString("lease_owner"))
                        assertNotNull(rs.getTimestamp("lease_expires_at"))
                    }
                }
            }
        }
    }
}
