package com.hakayat.backend.infra

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

object DatabaseConfig {
    fun dataSource(config: RuntimeConfig): DataSource? {
        val url = config.databaseUrl ?: return null
        return HikariDataSource(HikariConfig().apply {
            jdbcUrl = url
            maximumPoolSize = 10
            minimumIdle = 2
            poolName = "night-tales-postgres"
        })
    }
}
