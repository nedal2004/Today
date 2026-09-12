package com.n.alian.today.data.local

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromBucket(bucket: Bucket): String = bucket.name

    @TypeConverter
    fun toBucket(value: String): Bucket = Bucket.valueOf(value)

    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): Priority = Priority.valueOf(value)

    @TypeConverter
    fun fromCategory(category: Category): String = category.name

    @TypeConverter
    fun toCategory(value: String): Category = Category.valueOf(value)
}