package com.n.alian.today

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.n.alian.today.ui.theme.TodayTheme
import com.n.alian.today.ui.tasklist.TaskListScreen
import com.n.alian.today.ui.tasklist.TaskListViewModel
import com.n.alian.today.ui.tasklist.TaskListViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodayTheme {
                val repository = (application as TodayApp).repository
                val viewModel: TaskListViewModel = viewModel(
                    factory = TaskListViewModelFactory(
                        application = application,  // ← مرّرنا Application للـ Factory
                        repository = repository
                    )
                )

                val uiState by viewModel.uiState.collectAsState()

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    TaskListScreen(viewModel = viewModel)
                }
            }
        }
    }
}