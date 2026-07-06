package com.rubens.controletarefas.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeLogDao {
    @Query("SELECT * FROM time_logs WHERE dateString = :date")
    fun getTimeLogsForDate(date: String): Flow<List<TimeLog>>

    @Query("SELECT * FROM time_logs WHERE taskId = :taskId AND dateString = :date LIMIT 1")
    suspend fun getTimeLog(taskId: Long, date: String): TimeLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(timeLog: TimeLog)

    @Query("SELECT dateString, SUM(durationMillis) as totalDuration FROM time_logs GROUP BY dateString ORDER BY dateString DESC LIMIT 7")
    fun getDailyTotalsForLast7Days(): Flow<List<DailyTotal>>

    @Query("SELECT * FROM time_logs")
    fun getAllTimeLogs(): Flow<List<TimeLog>>

    @Query("DELETE FROM time_logs")
    suspend fun deleteAllTimeLogs()
}
