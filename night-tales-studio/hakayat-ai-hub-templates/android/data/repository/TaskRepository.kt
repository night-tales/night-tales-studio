package com.hakayat.aihub.data.repository

import com.hakayat.aihub.data.local.dao.TaskDao
import com.hakayat.aihub.data.local.entity.TaskEntity
import com.hakayat.aihub.data.remote.HakayatApi
import com.hakayat.aihub.data.remote.TaskRequestApi
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val api: HakayatApi
) {

    // قراءة المهام من قاعدة البيانات المحلية (Offline-First)
    fun observeTasks(): Flow<List<TaskEntity>> = taskDao.observeAllTasks()

    // إرسال مهمة جديدة
    suspend fun submitNewTask(prompt: String, agentId: String): Result<String> {
        val localTaskId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        // 1. حفظ المهمة محلياً بحالة PENDING
        val localTask = TaskEntity(
            id = localTaskId,
            agentId = agentId,
            prompt = prompt,
            status = "PENDING",
            result = null,
            progress = 0f,
            createdAt = now,
            updatedAt = now
        )
        taskDao.insertTask(localTask)

        return try {
            // 2. إرسال الطلب للخادم (Ktor Backend)
            val response = api.submitTask(TaskRequestApi(prompt, agentId))
            
            // 3. تحديث الحالة محلياً بالمعرف الفعلي والحالة الجديدة
            taskDao.updateTaskStatus(
                taskId = localTaskId, 
                status = response.status, 
                progress = 0.1f, 
                result = null
            )
            Result.success(response.id)
        } catch (e: Exception) {
            // في حال فشل الإرسال، تبقى المهمة محفوظة محلياً ويمكن إعادة محاولة إرسالها (WorkManager)
            taskDao.updateTaskStatus(
                taskId = localTaskId,
                status = "FAILED",
                progress = 0f,
                result = "فشل الاتصال بالخادم: ${e.message}"
            )
            Result.failure(e)
        }
    }
}
