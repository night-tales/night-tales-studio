package com.hakayat.studio.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class GenerationWork(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val projectId = inputData.getString("projectId") ?: return Result.failure()
        return if (projectId.isNotBlank()) Result.success() else Result.retry()
    }
}
