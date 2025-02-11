package com.example.libraryapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val favoriteGenres: String,
    val interests: String,
    val lastReadBook: String
)