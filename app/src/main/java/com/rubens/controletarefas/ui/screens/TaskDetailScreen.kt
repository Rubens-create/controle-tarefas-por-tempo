package com.rubens.controletarefas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rubens.controletarefas.ui.components.ChecklistSection
import com.rubens.controletarefas.ui.components.TagSelector
import com.rubens.controletarefas.ui.components.TimerDisplay
import com.rubens.controletarefas.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: Long,
    viewModel: TaskViewModel,
    onBack: () -> Unit
) {
    val tasksWithTime by viewModel.tasksWithTodayTime.collectAsStateWithLifecycle()
    val activeTaskId by viewModel.activeTaskId.collectAsStateWithLifecycle()
    val customTags by viewModel.customTags.collectAsStateWithLifecycle()
    val checklistItems by viewModel.getChecklistItems(taskId).collectAsStateWithLifecycle(initialValue = emptyList())

    val item = tasksWithTime.find { it.taskWithChecklist.task.id == taskId }
    val taskWithChecklist = item?.taskWithChecklist
    val task = taskWithChecklist?.task

    if (task == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Tarefa nao encontrada")
        }
        return
    }

    val isActive = task.id == activeTaskId
    val displayTime = item?.todayTimeMillis ?: 0L

    var editTitle by remember(task.id) { mutableStateOf(task.title) }
    var editDescription by remember(task.id) { mutableStateOf(task.description) }
    var editTag by remember(task.id) { mutableStateOf(task.tag) }
    val initialGoalHours = if (task.dailyGoalMinutes > 0) (task.dailyGoalMinutes.toFloat() / 60f).toString() else ""
    var editGoalHours by remember(task.id) { mutableStateOf(initialGoalHours) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes") },
                navigationIcon = {
                    IconButton(onClick = {
                        // Save changes before going back
                        val minutes = editGoalHours.replace(',', '.').toFloatOrNull()?.let { (it * 60).toInt() } ?: 0
                        viewModel.updateTask(
                            task.copy(
                                title = editTitle,
                                description = editDescription,
                                tag = editTag,
                                dailyGoalMinutes = minutes
                            )
                        )
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Timer section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimerDisplay(
                        formattedTime = viewModel.formatTime(displayTime),
                        isActive = isActive,
                        large = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.toggleTask(taskId) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth(0.6f)
                    ) {
                        Icon(
                            imageVector = if (isActive) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isActive) "Pausar" else "Iniciar")
                    }
                }
            }

            // Title
            OutlinedTextField(
                value = editTitle,
                onValueChange = { editTitle = it },
                label = { Text("Titulo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Description
            OutlinedTextField(
                value = editDescription,
                onValueChange = { editDescription = it },
                label = { Text("Descricao") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 5,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Meta Diária
            OutlinedTextField(
                value = editGoalHours,
                onValueChange = { editGoalHours = it },
                label = { Text("Meta diaria (em horas, ex: 1.5 ou 2)") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Tag selector
            Column {
                Text(
                    text = "Tag",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                TagSelector(
                    tags = customTags,
                    selectedTag = editTag,
                    onTagSelected = { editTag = it }
                )
            }

            // Checklist
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            ChecklistSection(
                items = checklistItems,
                onAddItem = { text -> viewModel.addChecklistItem(taskId, text) },
                onToggleItem = { item -> viewModel.toggleChecklistItem(item) },
                onDeleteItem = { item -> viewModel.deleteChecklistItem(item) }
            )
        }
    }
}
