package com.hakayat.backend.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        val config = HikariConfig().apply {
            // في الإنتاج، تُقرأ هذه القيم من متغيرات البيئة (Environment Variables)
            jdbcUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/hakayat"
            username = System.getenv("DATABASE_USER") ?: "hakayat"
            password = System.getenv("DATABASE_PASSWORD") ?: "hakayat_local"
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_READ_COMMITTED"
            validate()
        }
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)

        // تهيئة الجداول (لأغراض التطوير فقط، استخدم Flyway في الإنتاج)
        transaction {
            SchemaUtils.createMissingTablesAndColumns(TasksTable)
        }
    }
}
