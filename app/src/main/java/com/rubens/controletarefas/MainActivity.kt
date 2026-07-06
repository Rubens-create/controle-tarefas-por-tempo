package com.rubens.controletarefas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.rubens.controletarefas.ui.navigation.AppNavigation
import com.rubens.controletarefas.ui.theme.ControleTarefasTheme
import com.rubens.controletarefas.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControleTarefasTheme {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}
