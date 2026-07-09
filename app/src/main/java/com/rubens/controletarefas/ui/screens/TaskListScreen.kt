package com.rubens.controletarefas.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rubens.controletarefas.data.TaskWithChecklist
import com.rubens.controletarefas.ui.components.AddTaskDialog
import com.rubens.controletarefas.ui.components.TaskCard
import com.rubens.controletarefas.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    onTaskClick: (Long) -> Unit
) {
    val tasksWithTime by viewModel.tasksWithTodayTime.collectAsStateWithLifecycle()
    val activeTaskId by viewModel.activeTaskId.collectAsStateWithLifecycle()
    val customTags by viewModel.customTags.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tarefas",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar tarefa")
            }
        }
    ) { paddingValues ->
        if (tasksWithTime.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhuma tarefa criada.\nToque no + para adicionar.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = tasksWithTime,
                    key = { it.taskWithChecklist.task.id }
                ) { item ->
                    val taskWithChecklist = item.taskWithChecklist
                    val isActive = taskWithChecklist.task.id == activeTaskId
                    val displayTime = viewModel.formatTime(item.todayTimeMillis)

                    SwipeToDismissBox(
                        state = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteTask(taskWithChecklist.task)
                                    true
                                } else false
                            }
                        ),
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "Excluir",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        },
                        enableDismissFromStartToEnd = false
                    ) {
                    val isGoalActive = viewModel.isGoalActiveToday(taskWithChecklist.task)
                    val displayGoalMins = if (isGoalActive) taskWithChecklist.task.dailyGoalMinutes else 0
                    TaskCard(
                        title = taskWithChecklist.task.title,
                        formattedTime = displayTime,
                        isActive = isActive,
                        tag = taskWithChecklist.task.tag,
                        checklistTotal = taskWithChecklist.checklistItems.size,
                        checklistCompleted = taskWithChecklist.checklistItems.count { it.isCompleted },
                        dailyGoalMinutes = displayGoalMins,
                        todayTimeMillis = item.todayTimeMillis,
                        onToggleTimer = { viewModel.toggleTask(taskWithChecklist.task.id) },
                        onClick = { onTaskClick(taskWithChecklist.task.id) }
                    )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            tags = customTags,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, description, tag, dailyGoalMinutes, days, months ->
                viewModel.addTask(title, description, tag, dailyGoalMinutes, days, months)
                showAddDialog = false
            }
        )
    }
}
