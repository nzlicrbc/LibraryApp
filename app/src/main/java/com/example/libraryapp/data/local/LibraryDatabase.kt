package com.example.libraryapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.libraryapp.data.local.converter.Converters
import com.example.libraryapp.data.local.dao.SavedBooksDao
import com.example.libraryapp.data.local.dao.UserPreferencesDao
import com.example.libraryapp.data.local.entity.SavedBookEntity
import com.example.libraryapp.data.local.entity.UserPreferencesEntity

@Database(
    entities = [UserPreferencesEntity::class, SavedBookEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LibraryDatabase : RoomDatabase() {
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun savedBooksDao(): SavedBooksDao

    companion object {
        const val DATABASE_NAME = "library_database"
    }
}