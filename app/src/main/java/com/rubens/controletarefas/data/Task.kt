package com.rubens.controletarefas.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val elapsedTimeMillis: Long = 0L,
    val isActive: Boolean = false,
    val tag: String = "",
    val dailyGoalMinutes: Int = 0, // Meta diária em minutos (0 = sem meta)
    val createdAt: Long = System.currentTimeMillis()
)
