package com.bookfinder.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bookfinder.app.data.local.dao.BookDao
import com.bookfinder.app.data.local.entity.BookEntity

@Database(
    entities = [BookEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
}


