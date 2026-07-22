package com.n.alian.today.ui.tasklist

import com.n.alian.today.data.local.Bucket
import com.n.alian.today.data.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = TaskListViewModel(TaskRepository(FakeTaskDao()))

    @Test
    fun addingTask_appendsToSelectedBucket() = runTest(testDispatcher) {
        val viewModel = viewModel()
        val states = mutableListOf<TaskListUiState>()
        backgroundScope.launch { viewModel.uiState.collect { states.add(it) } }

        viewModel.onAddTask("Buy milk")

        val latest = states.last()
        assertEquals(1, latest.tasks.size)
        assertEquals("Buy milk", latest.tasks.first().title)
        assertEquals(Bucket.TODAY, latest.tasks.first().bucket)
    }

    @Test
    fun addingBlankTask_isIgnored() = runTest(testDispatcher) {
        val viewModel = viewModel()
        val states = mutableListOf<TaskListUiState>()
        backgroundScope.launch { viewModel.uiState.collect { states.add(it) } }

        viewModel.onAddTask("   ")

        assertTrue(states.last().tasks.isEmpty())
    }

    @Test
    fun completingTask_thenUndo_restoresActiveTask() = runTest(testDispatcher) {
        val viewModel = viewModel()
        val states = mutableListOf<TaskListUiState>()
        backgroundScope.launch { viewModel.uiState.collect { states.add(it) } }

        viewModel.onAddTask("Read a book")
        val added = states.last().tasks.first()

        viewModel.onComplete(added)
        assertTrue(states.last().tasks.isEmpty())

        viewModel.onUndo()
        assertEquals(1, states.last().tasks.size)
        assertFalse(states.last().tasks.first().isDone)
    }

    @Test
    fun switchingBucket_showsOnlyThatBucketsTasks() = runTest(testDispatcher) {
        val viewModel = viewModel()
        val states = mutableListOf<TaskListUiState>()
        backgroundScope.launch { viewModel.uiState.collect { states.add(it) } }

        viewModel.onAddTask("Today task")
        viewModel.onBucketSelected(Bucket.LATER)
        viewModel.onAddTask("Later task")

        assertEquals(Bucket.LATER, states.last().selectedBucket)
        assertEquals(1, states.last().tasks.size)
        assertEquals("Later task", states.last().tasks.first().title)

        viewModel.onBucketSelected(Bucket.TODAY)
        assertEquals(1, states.last().tasks.size)
        assertEquals("Today task", states.last().tasks.first().title)
    }
}
