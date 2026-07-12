package com.n.alian.today.data.local

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromBucket(bucket: Bucket): String = bucket.name

    @TypeConverter
    fun toBucket(value: String): Bucket = Bucket.valueOf(value)
}