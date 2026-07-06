package com.rubens.controletarefas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rubens.controletarefas.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TaskViewModel
) {
    val tasks by viewModel.allTasksWithChecklist.collectAsStateWithLifecycle()

    val totalTimeMs = tasks.sumOf { it.task.elapsedTimeMillis }
    val totalTasks = tasks.size
    val totalChecklist = tasks.sumOf { it.checklistItems.size }
    val completedChecklist = tasks.sumOf { twc -> twc.checklistItems.count { it.isCompleted } }

    // Group time by tag
    val timeByTag = tasks
        .filter { it.task.tag.isNotBlank() }
        .groupBy { it.task.tag }
        .mapValues { entry -> entry.value.sumOf { it.task.elapsedTimeMillis } }
        .toList()
        .sortedByDescending { it.second }

    val untaggedTime = tasks
        .filter { it.task.tag.isBlank() }
        .sumOf { it.task.elapsedTimeMillis }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.headlineMedium
                    )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    icon = Icons.Outlined.AccessTime,
                    label = "Tempo Total",
                    value = viewModel.formatTime(totalTimeMs),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    icon = Icons.Outlined.TaskAlt,
                    label = "Tarefas",
                    value = totalTasks.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            // Checklist progress
            if (totalChecklist > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Progresso do Checklist",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { completedChecklist.toFloat() / totalChecklist.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$completedChecklist de $totalChecklist itens concluidos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Time by tag
            Text(
                text = "Tempo por Tag",
                style = MaterialTheme.typography.titleLarge
            )

            if (timeByTag.isEmpty() && untaggedTime == 0L) {
                Text(
                    text = "Nenhum dado de tempo registrado ainda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val maxTime = maxOf(
                    timeByTag.maxOfOrNull { it.second } ?: 0L,
                    untaggedTime
                ).coerceAtLeast(1L)

                timeByTag.forEach { (tag, timeMs) ->
                    TagTimeBar(
                        tag = tag,
                        timeMs = timeMs,
                        maxTimeMs = maxTime,
                        formatTime = { viewModel.formatTime(it) }
                    )
                }

                if (untaggedTime > 0L) {
                    TagTimeBar(
                        tag = "Sem tag",
                        timeMs = untaggedTime,
                        maxTimeMs = maxTime,
                        formatTime = { viewModel.formatTime(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TagTimeBar(
    tag: String,
    timeMs: Long,
    maxTimeMs: Long,
    formatTime: (Long) -> String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = tag,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = formatTime(timeMs),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (timeMs.toFloat() / maxTimeMs.toFloat()).coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
