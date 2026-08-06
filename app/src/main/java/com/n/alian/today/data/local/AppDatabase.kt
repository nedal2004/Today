package com.n.alian.today.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
//  singleton
@Database(entities = [Task::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase(){
    abstract fun taskDao(): TaskDao
    companion object{
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get (context: Context) : AppDatabase =
            INSTANCE?: synchronized(this){
                //للامان
                INSTANCE?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "today.db"
                ).build().also { INSTANCE=it }
            }
    }

}