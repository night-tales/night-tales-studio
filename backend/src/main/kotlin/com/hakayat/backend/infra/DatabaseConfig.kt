package com.hakayat.backend.infra

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import javax.sql.DataSource

object DatabaseConfig {
    fun dataSource(config: RuntimeConfig): HikariDataSource? {
        val url = config.databaseUrl?.trim()?.takeIf(String::isNotEmpty) ?: return null
        return HikariDataSource(HikariConfig().apply {
            jdbcUrl = normalizeJdbcUrl(url)
            maximumPoolSize = 10
            minimumIdle = 1
            connectionTimeout = 10_000
            validationTimeout = 5_000
            poolName = "night-tales-postgres"
        })
    }

    fun migrate(dataSource: DataSource) {
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load()
            .migrate()
    }

    private fun normalizeJdbcUrl(url: String): String = when {
        url.startsWith("jdbc:postgresql://") -> url
        url.startsWith("postgresql://") -> "jdbc:$url"
        url.startsWith("postgres://") -> "jdbc:postgresql://${url.removePrefix("postgres://")}" 
        else -> error("DATABASE_URL must be a PostgreSQL connection URL")
    }
}
