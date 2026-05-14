package com.example.libraryapp.di

import android.content.Context
import androidx.room.Room
import com.example.libraryapp.data.local.LibraryDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LibraryDatabase {
        return Room.databaseBuilder(
            context,
            LibraryDatabase::class.java,
            LibraryDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideSavedBooksDao(database: LibraryDatabase) = database.savedBooksDao()
}