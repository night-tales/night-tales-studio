package com.hakayat.aihub.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hakayat.aihub.data.local.dao.TaskDao
import com.hakayat.aihub.data.local.entity.TaskEntity

@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
