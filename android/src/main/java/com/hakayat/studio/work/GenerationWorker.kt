package com.hakayat.studio.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class GenerationWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        // Long-running generation is backend-owned; Android observes and syncs job state.
        return Result.success()
    }
}
