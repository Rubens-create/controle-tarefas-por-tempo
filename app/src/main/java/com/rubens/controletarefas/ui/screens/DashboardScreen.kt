package com.rubens.controletarefas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rubens.controletarefas.data.DailyTotal
import com.rubens.controletarefas.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TaskViewModel
) {
    val tasksWithTime by viewModel.tasksWithTodayTime.collectAsStateWithLifecycle()
    val dailyTotals by viewModel.dailyTotals.collectAsStateWithLifecycle()

    // Métricas diárias (Tempo de hoje)
    val totalTimeTodayMs = tasksWithTime.sumOf { it.todayTimeMillis }
    val totalTasksToday = tasksWithTime.count { it.todayTimeMillis > 0 }
    val totalChecklist = tasksWithTime.sumOf { it.taskWithChecklist.checklistItems.size }
    val completedChecklist = tasksWithTime.sumOf { it.taskWithChecklist.checklistItems.count { it.isCompleted } }

    // Agrupamento do tempo por tag (apenas HOJE)
    val timeByTagToday = tasksWithTime
        .filter { it.taskWithChecklist.task.tag.isNotBlank() }
        .groupBy { it.taskWithChecklist.task.tag }
        .mapValues { entry -> entry.value.sumOf { it.todayTimeMillis } }
        .toList()
        .sortedByDescending { it.second }

    val untaggedTimeToday = tasksWithTime
        .filter { it.taskWithChecklist.task.tag.isBlank() }
        .sumOf { it.todayTimeMillis }

    // Tag mais produtiva de hoje
    val mostProductiveTag = timeByTagToday.firstOrNull()?.first ?: if (untaggedTimeToday > 0) "Sem tag" else "Nenhuma"

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
            // Seção de Destaque Diário
            Text(
                text = "Hoje",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Cards de Sumário Diário
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    icon = Icons.Outlined.AccessTime,
                    label = "Tempo Hoje",
                    value = viewModel.formatTime(totalTimeTodayMs),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    icon = Icons.Outlined.TaskAlt,
                    label = "Tarefas Ativas",
                    value = totalTasksToday.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            // Card da tag destaque
            if (timeByTagToday.isNotEmpty() || untaggedTimeToday > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Label,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Tag Destaque de Hoje",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = mostProductiveTag,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Gráfico de Produtividade Semanal (Histórico de 7 dias)
            ProductivityChart(dailyTotals = dailyTotals)

            // Progresso do Checklist
            if (totalChecklist > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder()
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
                            trackColor = MaterialTheme.colorScheme.outlineVariant
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

            // Tempo por Tag (HOJE)
            Text(
                text = "Tempo por Tag (Hoje)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (timeByTagToday.isEmpty() && untaggedTimeToday == 0L) {
                Text(
                    text = "Nenhum dado registrado para o dia de hoje.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val maxTime = maxOf(
                    timeByTagToday.maxOfOrNull { it.second } ?: 0L,
                    untaggedTimeToday
                ).coerceAtLeast(1L)

                timeByTagToday.forEach { (tag, timeMs) ->
                    TagTimeBar(
                        tag = tag,
                        timeMs = timeMs,
                        maxTimeMs = maxTime,
                        formatTime = { viewModel.formatTime(it) }
                    )
                }

                if (untaggedTimeToday > 0L) {
                    TagTimeBar(
                        tag = "Sem tag",
                        timeMs = untaggedTimeToday,
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
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
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
            val tagColor = com.rubens.controletarefas.ui.theme.getTagColors(tag).content
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (timeMs.toFloat() / maxTimeMs.toFloat()).coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(6.dp))
                    .background(tagColor)
            )
        }
    }
}

@Composable
fun ProductivityChart(
    dailyTotals: List<DailyTotal>,
    modifier: Modifier = Modifier
) {
    val sortedTotals = dailyTotals.sortedBy { it.dateString }.takeLast(7)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Produtividade Semanal (Horas)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (sortedTotals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum historico de tempo nesta semana.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val maxDuration = sortedTotals.maxOfOrNull { it.totalDuration }?.coerceAtLeast(1L) ?: 1L
                val maxHours = maxDuration.toFloat() / 3600000f

                Row(
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    sortedTotals.forEach { total ->
                        val hours = total.totalDuration.toFloat() / 3600000f
                        val heightFraction = if (maxHours > 0) hours / maxHours else 0f

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = "%.1fh".format(hours),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(fraction = heightFraction.coerceIn(0.08f, 1f))
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val formattedLabel = try {
                                val parts = total.dateString.split("-")
                                "${parts[2]}/${parts[1]}"
                            } catch (e: Exception) {
                                total.dateString
                            }
                            Text(
                                text = formattedLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
