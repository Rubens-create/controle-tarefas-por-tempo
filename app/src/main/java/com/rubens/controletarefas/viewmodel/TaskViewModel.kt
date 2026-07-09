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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = TaskRepository(
        database.taskDao(),
        database.checklistItemDao(),
        database.timeLogDao()
    )

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

    // Controle de data de hoje
    val todayDate = MutableStateFlow(getTodayDateString())

    // Logs de tempo de hoje
    val todayTimeLogs: StateFlow<List<TimeLog>> = todayDate
        .flatMapLatest { date -> repository.getTimeLogsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combina as tarefas com o tempo decorrido especificamente HOJE
    val tasksWithTodayTime: StateFlow<List<TaskWithTodayTime>> = combine(
        allTasksWithChecklist,
        todayTimeLogs,
        _activeTaskId,
        _activeElapsedTime
    ) { tasks, logs, activeId, activeTime ->
        tasks.map { twc ->
            val logForTask = logs.find { it.taskId == twc.task.id }
            val baseTime = logForTask?.durationMillis ?: 0L
            val finalTime = if (twc.task.id == activeId) {
                activeTime
            } else {
                baseTime
            }
            TaskWithTodayTime(twc, finalTime)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Totais diários de produtividade para o gráfico (últimos 7 dias)
    val dailyTotals: StateFlow<List<DailyTotal>> = repository.getDailyTotalsForLast7Days()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Lista de Tags Padrão ampliada e customizadas gerenciadas nas configurações
    private val _customTags = MutableStateFlow(
        listOf(
            "Trabalho", "Estudo", "Games", "Lazer", "Almoco",
            "Descanso", "Redes Sociais", "Exercicio", "Projetos",
            "Leitura", "Reuniao"
        )
    )
    val customTags: StateFlow<List<String>> = _customTags.asStateFlow()

    init {
        // Restaurar tarefa ativa na inicialização do app
        viewModelScope.launch {
            val activeTask = repository.getActiveTask()
            if (activeTask != null) {
                // Checar se temos um log para hoje
                val todayLog = repository.getTimeLogsForDate(getTodayDateString()).first().find { it.taskId == activeTask.id }
                _activeTaskId.value = activeTask.id
                timerBaseElapsed = todayLog?.durationMillis ?: 0L
                startTimerInternal()
            }
        }
    }

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun addTask(
        title: String,
        description: String = "",
        tag: String = "",
        dailyGoalMinutes: Int = 0,
        goalDaysOfWeek: String = "1,2,3,4,5,6,7",
        goalMonths: Int = 0
    ) {
        viewModelScope.launch {
            repository.insertTask(
                Task(
                    title = title,
                    description = description,
                    tag = tag,
                    dailyGoalMinutes = dailyGoalMinutes,
                    goalDaysOfWeek = goalDaysOfWeek,
                    goalMonths = goalMonths,
                    goalStartTimestamp = System.currentTimeMillis()
                )
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
            // Atualizar data de hoje caso tenha mudado o dia enquanto o app rodava
            todayDate.value = getTodayDateString()

            if (_activeTaskId.value == taskId) {
                // Pausar tarefa ativa
                stopTimer()
                repository.deactivateAllTasks()
                _activeTaskId.value = null
            } else {
                // Salvar tempo acumulado da tarefa ativa anterior antes de trocar
                val currentActiveId = _activeTaskId.value
                if (currentActiveId != null) {
                    flushSessionTime(currentActiveId, _activeElapsedTime.value)
                    stopTimer()
                }
                
                // Ativar nova tarefa
                repository.activateTask(taskId)
                val todayLogs = repository.getTimeLogsForDate(getTodayDateString()).first()
                val logForTask = todayLogs.find { it.taskId == taskId }
                
                _activeTaskId.value = taskId
                timerBaseElapsed = logForTask?.durationMillis ?: 0L
                startTimerInternal()
            }
        }
    }

    private fun startTimerInternal() {
        timerJob?.cancel()
        timerStartRealtime = SystemClock.elapsedRealtime()
        var lastFlushRealtime = timerStartRealtime
        
        timerJob = viewModelScope.launch {
            while (isActive) {
                val now = SystemClock.elapsedRealtime()
                val elapsedThisSession = now - timerStartRealtime
                val totalElapsed = timerBaseElapsed + elapsedThisSession
                _activeElapsedTime.value = totalElapsed
                
                // Salvar/flush periodicamente no banco a cada 5 segundos para segurança contra quedas do app
                if (now - lastFlushRealtime >= 5000) {
                    val activeId = _activeTaskId.value
                    if (activeId != null) {
                        flushSessionTime(activeId, totalElapsed)
                    }
                    lastFlushRealtime = now
                }
                delay(50L)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        val currentActiveId = _activeTaskId.value
        if (currentActiveId != null) {
            val totalElapsed = _activeElapsedTime.value
            viewModelScope.launch {
                flushSessionTime(currentActiveId, totalElapsed)
            }
        }
        _activeElapsedTime.value = 0L
    }

    // Grava o tempo acumulado no TimeLog do dia corrente
    private suspend fun flushSessionTime(taskId: Long, totalTime: Long) {
        val today = getTodayDateString()
        // O tempo do log de hoje deve ser atualizado para ser exatamente o totalTime atualizado da tarefa
        val todayLogs = repository.getTimeLogsForDate(today).first()
        val existingLog = todayLogs.find { it.taskId == taskId }
        
        if (existingLog != null) {
            // Atualiza para o novo tempo total
            database.timeLogDao().insertOrUpdate(existingLog.copy(durationMillis = totalTime))
        } else {
            // Cria um novo log com o tempo acumulado
            database.timeLogDao().insertOrUpdate(
                TimeLog(taskId = taskId, dateString = today, durationMillis = totalTime)
            )
        }
    }

    // Checklist
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

    // Tags
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
            repository.deleteAllTimeLogs()
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

    fun isGoalActiveToday(task: Task): Boolean {
        if (task.dailyGoalMinutes <= 0) return false
        
        // Verifica dia da semana (1=Dom, 2=Seg, ..., 7=Sab)
        val calendar = Calendar.getInstance()
        val todayDayOfWeekNum = calendar.get(Calendar.DAY_OF_WEEK)
        val activeDays = task.goalDaysOfWeek.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        if (!activeDays.contains(todayDayOfWeekNum)) return false
        
        // Verifica limite de meses
        if (task.goalMonths > 0 && task.goalStartTimestamp > 0) {
            val startCalendar = Calendar.getInstance().apply {
                timeInMillis = task.goalStartTimestamp
            }
            val endCalendar = Calendar.getInstance().apply {
                timeInMillis = task.goalStartTimestamp
                add(Calendar.MONTH, task.goalMonths)
            }
            val today = Calendar.getInstance()
            if (today.before(startCalendar) || today.after(endCalendar)) {
                return false
            }
        }
        
        return true
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
