package com.hakayat.backend.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

object DatabaseFactory {
    private lateinit var dataSource: HikariDataSource

    fun initialize() {
        if (::dataSource.isInitialized) return

        val jdbcUrl = System.getenv("JDBC_DATABASE_URL")
            ?: error("JDBC_DATABASE_URL must be configured")
        val username = System.getenv("DATABASE_USER")
            ?: error("DATABASE_USER must be configured")
        val password = System.getenv("DATABASE_PASSWORD")
            ?: error("DATABASE_PASSWORD must be configured")

        val config = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = username
            this.password = password
            maximumPoolSize = (System.getenv("DATABASE_POOL_SIZE") ?: "10").toInt()
            minimumIdle = 1
            connectionTimeout = 10_000
            validationTimeout = 5_000
        }

        dataSource = HikariDataSource(config)
    }

    fun <T> transaction(block: (java.sql.Connection) -> T): T {
        check(::dataSource.isInitialized) { "DatabaseFactory.initialize() must be called first" }
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            return try {
                val result = block(connection)
                connection.commit()
                result
            } catch (e: Exception) {
                connection.rollback()
                throw e
            }
        }
    }
}
