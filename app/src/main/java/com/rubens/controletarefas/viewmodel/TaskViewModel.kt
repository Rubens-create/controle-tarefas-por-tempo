package com.rubens.controletarefas.viewmodel

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rubens.controletarefas.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = TaskRepository(database.taskDao(), database.checklistItemDao())

    val allTasksWithChecklist: StateFlow<List<TaskWithChecklist>> =
        repository.allTasksWithChecklist
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTags: StateFlow<List<String>> =
        repository.allTags
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeTaskId = MutableStateFlow<Long?>(null)
    val activeTaskId: StateFlow<Long?> = _activeTaskId.asStateFlow()

    private val _activeElapsedTime = MutableStateFlow(0L)
    val activeElapsedTime: StateFlow<Long> = _activeElapsedTime.asStateFlow()

    private var timerJob: Job? = null
    private var timerStartRealtime: Long = 0L
    private var timerBaseElapsed: Long = 0L

    private val _customTags = MutableStateFlow(
        listOf("Trabalho", "Estudo", "Games", "Lazer", "Almoco", "Descanso", "Redes Sociais")
    )
    val customTags: StateFlow<List<String>> = _customTags.asStateFlow()

    init {
        // Restore active task on app start
        viewModelScope.launch {
            val activeTask = repository.getActiveTask()
            if (activeTask != null) {
                _activeTaskId.value = activeTask.id
                timerBaseElapsed = activeTask.elapsedTimeMillis
                startTimerInternal()
            }
        }
    }

    fun addTask(title: String, description: String = "", tag: String = "") {
        viewModelScope.launch {
            repository.insertTask(
                Task(title = title, description = description, tag = tag)
            )
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            if (_activeTaskId.value == task.id) {
                stopTimer()
            }
            repository.deleteTask(task)
        }
    }

    fun toggleTask(taskId: Long) {
        viewModelScope.launch {
            if (_activeTaskId.value == taskId) {
                // Deactivate current task
                stopTimer()
                repository.deactivateAllTasks()
                _activeTaskId.value = null
            } else {
                // Save current task's elapsed time
                val currentActiveId = _activeTaskId.value
                if (currentActiveId != null) {
                    val elapsed = _activeElapsedTime.value
                    repository.updateElapsedTime(currentActiveId, elapsed)
                    stopTimer()
                }
                // Activate new task
                repository.activateTask(taskId)
                val task = repository.getTaskById(taskId)
                _activeTaskId.value = taskId
                timerBaseElapsed = task?.elapsedTimeMillis ?: 0L
                startTimerInternal()
            }
        }
    }

    private fun startTimerInternal() {
        timerJob?.cancel()
        timerStartRealtime = SystemClock.elapsedRealtime()
        timerJob = viewModelScope.launch {
            while (isActive) {
                val now = SystemClock.elapsedRealtime()
                _activeElapsedTime.value = timerBaseElapsed + (now - timerStartRealtime)
                delay(50L)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        val currentActiveId = _activeTaskId.value
        if (currentActiveId != null) {
            val elapsed = _activeElapsedTime.value
            viewModelScope.launch {
                repository.updateElapsedTime(currentActiveId, elapsed)
            }
        }
        _activeElapsedTime.value = 0L
    }

    // Checklist operations
    fun getChecklistItems(taskId: Long): Flow<List<ChecklistItem>> =
        repository.getChecklistItemsForTask(taskId)

    fun addChecklistItem(taskId: Long, text: String) {
        viewModelScope.launch {
            repository.insertChecklistItem(
                ChecklistItem(taskId = taskId, text = text)
            )
        }
    }

    fun toggleChecklistItem(item: ChecklistItem) {
        viewModelScope.launch {
            repository.updateChecklistItem(item.copy(isCompleted = !item.isCompleted))
        }
    }

    fun deleteChecklistItem(item: ChecklistItem) {
        viewModelScope.launch {
            repository.deleteChecklistItem(item)
        }
    }

    fun getTaskWithChecklist(taskId: Long): Flow<TaskWithChecklist?> = flow {
        emit(repository.getTaskWithChecklistById(taskId))
    }

    // Tag management
    fun addCustomTag(tag: String) {
        val current = _customTags.value.toMutableList()
        if (tag.isNotBlank() && !current.contains(tag)) {
            current.add(tag)
            _customTags.value = current
        }
    }

    fun removeCustomTag(tag: String) {
        _customTags.value = _customTags.value.filter { it != tag }
    }

    fun resetAllData() {
        viewModelScope.launch {
            stopTimer()
            _activeTaskId.value = null
            // Delete all tasks (cascade deletes checklist items)
            allTasksWithChecklist.value.forEach { twc ->
                repository.deleteTask(twc.task)
            }
        }
    }

    fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        // Save elapsed time when ViewModel is cleared
        val currentActiveId = _activeTaskId.value
        if (currentActiveId != null && timerJob?.isActive == true) {
            val elapsed = _activeElapsedTime.value
            // We can't use viewModelScope here as it's cancelled
            // The time will be saved next time the app starts
        }
        timerJob?.cancel()
    }
}
