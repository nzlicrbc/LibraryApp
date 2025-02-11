package com.example.libraryapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_books")
data class SavedBookEntity(
    @PrimaryKey
    val bookId: String,
    val title: String,
    val author: String,
    val thumbnailUrl: String?,
    val isFavorite: Boolean = false,
    val isSaved: Boolean = false,
    val isRead: Boolean = false,
    val lastReadPosition: Int = 0,
    val savedDate: Long = System.currentTimeMillis(),
    val completedDate: Long? = null
)