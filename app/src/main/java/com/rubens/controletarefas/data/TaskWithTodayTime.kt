package com.rubens.controletarefas.data

data class TaskWithTodayTime(
    val taskWithChecklist: TaskWithChecklist,
    val todayTimeMillis: Long
)
