package com.n.alian.today.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String? = null,                    //  وصف اختياري
    val bucket: Bucket = Bucket.TODAY,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val sortOrder: Int = 0,
    val dueDate: Long? = null,                    //  تاريخ استحقاق اختياري (epoch millis)
    val priority: Priority = Priority.MEDIUM,     //  أولوية
    val category: Category = Category.NONE        //  تصنيف
)