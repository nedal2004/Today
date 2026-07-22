package com.n.alian.today.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * يتحقق من [TaskDao] على قاعدة بيانات Room حقيقية في الذاكرة، خصوصاً
 * استعلامات الترحيل اليومي التي تعتمد عليها WorkManager (أسبوع ٤).
 */
@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: TaskDao

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.taskDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertAndObserveActive_returnsInsertedTask() = runTest {
        dao.insert(Task(title = "Write report", bucket = Bucket.TODAY))

        val active = dao.observeActive(Bucket.TODAY).first()

        assertEquals(1, active.size)
        assertEquals("Write report", active.first().title)
    }

    @Test
    fun promoteTomorrowToToday_movesOnlyUnfinishedTomorrowTasks() = runTest {
        dao.insert(Task(title = "Tomorrow task", bucket = Bucket.TOMORROW))
        dao.insert(Task(title = "Done tomorrow task", bucket = Bucket.TOMORROW, isDone = true))

        dao.promoteTomorrowToToday()

        val today = dao.observeActive(Bucket.TODAY).first()
        assertEquals(1, today.size)
        assertEquals("Tomorrow task", today.first().title)
    }

    @Test
    fun archiveOldDone_deletesOnlyTasksCompletedBeforeStartOfDay() = runTest {
        val startOfDay = 1_000_000L
        dao.insert(
            Task(title = "Old done", bucket = Bucket.TODAY, isDone = true, completedAt = startOfDay - 1)
        )
        dao.insert(
            Task(title = "Recent done", bucket = Bucket.TODAY, isDone = true, completedAt = startOfDay + 1)
        )

        dao.archiveOldDone(startOfDay)

        val remainingDone = dao.observeDoneToday(0L).first()
        assertEquals(1, remainingDone.size)
        assertEquals("Recent done", remainingDone.first().title)
    }
}
