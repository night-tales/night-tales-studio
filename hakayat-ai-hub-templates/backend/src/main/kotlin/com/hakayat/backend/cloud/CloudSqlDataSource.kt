package com.hakayat.backend.cloud

interface CloudSqlDataSource : CloudDataStore {
    suspend fun healthCheck(): Boolean
}
