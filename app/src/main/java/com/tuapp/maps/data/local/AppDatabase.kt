package com.tuapp.maps.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tuapp.maps.data.model.Category
import com.tuapp.maps.data.model.SavedPoint

@Database(entities = [SavedPoint::class, Category::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun savedPointDao(): SavedPointDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "geopuntos.db"
                ).build().also { INSTANCE = it }
            }
    }
}
