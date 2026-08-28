package com.hakayat.backend.realtime

import com.hakayat.backend.db.DatabaseFactory
import java.security.MessageDigest
import java.util.UUID

class RealtimeTicketRepository {
    fun issue(userId: String): String {
        val token = UUID.randomUUID().toString() + UUID.randomUUID().toString()
        val hash = sha256(token)
        DatabaseFactory.transaction { connection ->
            connection.prepareStatement(
                "INSERT INTO realtime_tickets (user_id, token_hash, expires_at) VALUES (?, ?, NOW() + INTERVAL '60 seconds')"
            ).use { statement ->
                statement.setString(1, userId)
                statement.setString(2, hash)
                statement.executeUpdate()
            }
        }
        return token
    }

    fun consume(token: String): String? = DatabaseFactory.transaction { connection ->
        connection.prepareStatement(
            """
            UPDATE realtime_tickets
            SET used_at = NOW()
            WHERE token_hash = ?
              AND used_at IS NULL
              AND expires_at > NOW()
            RETURNING user_id
            """.trimIndent()
        ).use { statement ->
            statement.setString(1, sha256(token))
            statement.executeQuery().use { rs ->
                if (rs.next()) rs.getString("user_id") else null
            }
        }
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
