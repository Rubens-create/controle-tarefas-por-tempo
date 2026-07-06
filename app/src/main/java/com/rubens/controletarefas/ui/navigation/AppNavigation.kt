package com.rubens.controletarefas.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.rubens.controletarefas.ui.screens.*
import com.rubens.controletarefas.viewmodel.TaskViewModel
import kotlinx.serialization.Serializable

// Destinations
@Serializable
object ListaTarefas

@Serializable
object Dashboard

@Serializable
object Configuracoes

@Serializable
data class DetalhesTarefa(val tarefaId: Long)

data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: Any
)

@Composable
fun AppNavigation(viewModel: TaskViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        BottomNavItem(
            label = "Tarefas",
            selectedIcon = Icons.Filled.FormatListBulleted,
            unselectedIcon = Icons.Outlined.FormatListBulleted,
            route = ListaTarefas
        ),
        BottomNavItem(
            label = "Dashboard",
            selectedIcon = Icons.Filled.BarChart,
            unselectedIcon = Icons.Outlined.BarChart,
            route = Dashboard
        ),
        BottomNavItem(
            label = "Configuracoes",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            route = Configuracoes
        )
    )

    // Show bottom bar only on main tabs (not on detail screen)
    val showBottomBar = currentDestination?.hierarchy?.any { dest ->
        bottomNavItems.any { item ->
            dest.hasRoute(item.route::class)
        }
    } == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { dest ->
                            dest.hasRoute(item.route::class)
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ListaTarefas,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<ListaTarefas> {
                TaskListScreen(
                    viewModel = viewModel,
                    onTaskClick = { taskId ->
                        navController.navigate(DetalhesTarefa(tarefaId = taskId))
                    }
                )
            }
            composable<Dashboard> {
                DashboardScreen(viewModel = viewModel)
            }
            composable<Configuracoes> {
                SettingsScreen(viewModel = viewModel)
            }
            composable<DetalhesTarefa> { backStackEntry ->
                val route: DetalhesTarefa = backStackEntry.toRoute()
                TaskDetailScreen(
                    taskId = route.tarefaId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
