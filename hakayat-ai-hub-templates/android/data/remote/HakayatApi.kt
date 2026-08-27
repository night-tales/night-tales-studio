package com.hakayat.aihub.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// نماذج البيانات للـ API
data class TaskRequestApi(val prompt: String, val agentId: String)
data class TaskResponseApi(val id: String, val status: String, val result: String?)

interface HakayatApi {

    @POST("api/v1/tasks")
    suspend fun submitTask(@Body request: TaskRequestApi): TaskResponseApi

    @GET("api/v1/tasks/{id}/status")
    suspend fun getTaskStatus(@Path("id") taskId: String): TaskResponseApi
}
