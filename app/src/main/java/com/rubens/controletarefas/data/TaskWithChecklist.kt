package com.rubens.controletarefas.data

import androidx.room.Embedded
import androidx.room.Relation

data class TaskWithChecklist(
    @Embedded val task: Task,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val checklistItems: List<ChecklistItem>
)
