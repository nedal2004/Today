package com.n.alian.today

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.n.alian.today.ui.theme.TodayTheme
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.n.alian.today.data.local.Bucket
import com.n.alian.today.data.local.Task
import com.n.alian.today.ui.tasklist.TaskListViewModel
import com.n.alian.today.ui.tasklist.TaskListViewModelFactory
import com.n.alian.today.ui.tasklist.TaskListScreen
import com.n.alian.today.data.local.AppDatabase
import com.n.alian.today.data.repository.TaskRepository
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // اختبار مؤقت لأسبوع ١ — بنحذفه بأسبوع ٢
        val repo = (application as TodayApp).repository

        lifecycleScope.launch {
            repo.add(Task(title = "أول مهمة بالتطبيق 🎉", bucket = Bucket.TODAY))
            repo.add(Task(title = "مهمة بكرة", bucket = Bucket.TOMORROW))

            repo.activeTasks(Bucket.TODAY).collect { tasks ->
                Log.d("TODAY_TEST", "مهام اليوم: ${tasks.size}")
                tasks.forEach { Log.d("TODAY_TEST", "→ ${it.id}: ${it.title}") }
            }
        }
        enableEdgeToEdge()
        setContent {
            TodayTheme {
                val repository = TaskRepository(
                    AppDatabase.get(applicationContext).taskDao()
                )
                val viewModel: TaskListViewModel = viewModel(
                    factory = TaskListViewModelFactory(repository)
                )
                TaskListScreen(viewModel)
            }
        }
    }
}
