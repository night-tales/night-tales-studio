package com.hakayat.backend.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun init(): Database {
        val config = HikariConfig().apply {
            jdbcUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/hakayat"
            username = System.getenv("POSTGRES_USER") ?: "hakayat"
            password = System.getenv("POSTGRES_PASSWORD") ?: "change-me"
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 20
            minimumIdle = 4
            isAutoCommit = false
        }
        return Database.connect(HikariDataSource(config))
    }
}
