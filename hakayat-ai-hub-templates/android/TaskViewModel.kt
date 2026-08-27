package com.hakayat.aihub.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class TaskUiState(
    val isLoading: Boolean = false,
    val messages: List<String> = emptyList(),
    val currentInput: String = "",
    val availableAgents: List<String> = listOf("Gemini 1.5 Pro", "GPT-4o", "Claude 3.5 Sonnet"),
    val selectedAgent: String = "Gemini 1.5 Pro"
)

class TaskViewModel : ViewModel() {
    private val _state = MutableStateFlow(TaskUiState())
    val state: StateFlow<TaskUiState> = _state.asStateFlow()

    fun updateInput(text: String) { 
        _state.update { it.copy(currentInput = text) } 
    }
    
    fun selectAgent(agent: String) { 
        _state.update { it.copy(selectedAgent = agent) } 
    }

    fun submitTask() {
        val task = _state.value.currentInput
        if (task.isBlank()) return

        // إضافة رسالة المستخدم للواجهة
        _state.update { it.copy(
            isLoading = true,
            currentInput = "",
            messages = it.messages + "أنت: $task"
        )}

        // محاكاة الاتصال بالـ Backend (Ktor API)
        viewModelScope.launch {
            delay(1500) // محاكاة وقت المعالجة
            _state.update { it.copy(
                isLoading = false,
                messages = it.messages + "الوكيل [${it.selectedAgent}]: جاري العمل على تحليل المهمة وإعداد المخرجات..."
            )}
        }
    }
}
