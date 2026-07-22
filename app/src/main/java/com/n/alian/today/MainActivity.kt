package com.n.alian.today

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.n.alian.today.ui.theme.TodayTheme
import com.n.alian.today.ui.tasklist.TaskListViewModel
import com.n.alian.today.ui.tasklist.TaskListViewModelFactory
import com.n.alian.today.ui.tasklist.TaskListScreen
import com.n.alian.today.widget.TodayWidget

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // زر "+" على الودجت يفتح التطبيق مباشرة على حوار إضافة مهمة.
        val openAddDialogOnLaunch = intent?.getBooleanExtra(EXTRA_OPEN_ADD_DIALOG, false) ?: false

        setContent {
            TodayTheme {
                val repository = (application as TodayApp).repository
                val viewModel: TaskListViewModel = viewModel(
                    factory = TaskListViewModelFactory(
                        repository = repository,
                        owner = this,
                        onDataChanged = { TodayWidget().updateAll(applicationContext) }
                    )
                )

                // استدعاء الشاشة هنا هو ما يحل مشكلة الشاشة السوداء
                TaskListScreen(viewModel = viewModel, openAddDialogOnLaunch = openAddDialogOnLaunch)
            }
        }
    }

    companion object {
        const val EXTRA_OPEN_ADD_DIALOG = "extra_open_add_dialog"
    }
}
