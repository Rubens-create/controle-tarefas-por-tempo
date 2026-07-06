package com.rubens.controletarefas.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistItemDao {
    @Query("SELECT * FROM checklist_items WHERE taskId = :taskId ORDER BY sortOrder ASC")
    fun getChecklistItemsForTask(taskId: Long): Flow<List<ChecklistItem>>

    @Insert
    suspend fun insertChecklistItem(item: ChecklistItem): Long

    @Update
    suspend fun updateChecklistItem(item: ChecklistItem)

    @Delete
    suspend fun deleteChecklistItem(item: ChecklistItem)

    @Query("DELETE FROM checklist_items WHERE taskId = :taskId")
    suspend fun deleteAllChecklistItemsForTask(taskId: Long)
}
