package com.rubens.controletarefas.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Transaction
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasksWithChecklist(): Flow<List<TaskWithChecklist>>

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskWithChecklistById(id: Long): TaskWithChecklist?

    @Insert
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("UPDATE tasks SET isActive = 0")
    suspend fun deactivateAllTasks()

    @Query("UPDATE tasks SET isActive = 1 WHERE id = :taskId")
    suspend fun activateTask(taskId: Long)

    @Query("UPDATE tasks SET elapsedTimeMillis = :elapsedTime WHERE id = :taskId")
    suspend fun updateElapsedTime(taskId: Long, elapsedTime: Long)

    @Query("SELECT * FROM tasks WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveTask(): Task?

    @Query("SELECT * FROM tasks WHERE tag = :tag ORDER BY createdAt DESC")
    fun getTasksByTag(tag: String): Flow<List<Task>>

    @Query("SELECT DISTINCT tag FROM tasks WHERE tag != '' ORDER BY tag ASC")
    fun getAllTags(): Flow<List<String>>
}
