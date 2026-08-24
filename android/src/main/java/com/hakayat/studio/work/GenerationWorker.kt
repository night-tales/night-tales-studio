package com.hakayat.studio.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class GenerationWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val jobId = inputData.getString("job_id") ?: return Result.failure()
        // Long-running generation is backend-owned; Android observes and syncs job state.
        return Result.success(androidx.work.workDataOf("job_id" to jobId, "status" to "submitted"))
    }
}
