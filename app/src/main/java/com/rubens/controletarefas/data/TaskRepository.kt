package com.rubens.controletarefas.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val checklistItemDao: ChecklistItemDao
) {
    val allTasksWithChecklist: Flow<List<TaskWithChecklist>> = taskDao.getAllTasksWithChecklist()
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val allTags: Flow<List<String>> = taskDao.getAllTags()

    suspend fun getTaskById(id: Long): Task? = taskDao.getTaskById(id)

    suspend fun getTaskWithChecklistById(id: Long): TaskWithChecklist? = taskDao.getTaskWithChecklistById(id)

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun activateTask(taskId: Long) {
        taskDao.deactivateAllTasks()
        taskDao.activateTask(taskId)
    }

    suspend fun deactivateAllTasks() = taskDao.deactivateAllTasks()

    suspend fun updateElapsedTime(taskId: Long, elapsedTime: Long) =
        taskDao.updateElapsedTime(taskId, elapsedTime)

    suspend fun getActiveTask(): Task? = taskDao.getActiveTask()

    fun getTasksByTag(tag: String): Flow<List<Task>> = taskDao.getTasksByTag(tag)

    fun getChecklistItemsForTask(taskId: Long): Flow<List<ChecklistItem>> =
        checklistItemDao.getChecklistItemsForTask(taskId)

    suspend fun insertChecklistItem(item: ChecklistItem): Long =
        checklistItemDao.insertChecklistItem(item)

    suspend fun updateChecklistItem(item: ChecklistItem) =
        checklistItemDao.updateChecklistItem(item)

    suspend fun deleteChecklistItem(item: ChecklistItem) =
        checklistItemDao.deleteChecklistItem(item)

    suspend fun deleteAllChecklistItemsForTask(taskId: Long) =
        checklistItemDao.deleteAllChecklistItemsForTask(taskId)
}
