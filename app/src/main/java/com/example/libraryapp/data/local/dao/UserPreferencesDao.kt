package com.example.libraryapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.libraryapp.data.local.entity.UserPreferencesEntity

@Dao
interface UserPreferencesDao {
    @Query("SELECT * FROM user_preferences LIMIT 1")
    suspend fun getUserPreferences(): UserPreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(preferences: UserPreferencesEntity)

    @Update
    suspend fun updateUserPreferences(preferences: UserPreferencesEntity)
}