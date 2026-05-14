package com.example.libraryapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.libraryapp.data.local.converter.Converters
import com.example.libraryapp.data.local.dao.SavedBooksDao
import com.example.libraryapp.data.local.entity.SavedBookEntity

@Database(
    entities = [SavedBookEntity::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LibraryDatabase : RoomDatabase() {
    abstract fun savedBooksDao(): SavedBooksDao

    companion object {
        const val DATABASE_NAME = "library_database"
    }
}