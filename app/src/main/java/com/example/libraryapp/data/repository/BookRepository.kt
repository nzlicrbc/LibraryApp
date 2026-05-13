package com.example.libraryapp.data.repository

import com.example.libraryapp.data.local.entity.SavedBookEntity
import com.example.libraryapp.data.remote.model.GoogleBook
import com.example.libraryapp.ui.airecommend.model.UserPreferences

interface BookRepository {
    suspend fun searchBooks(
        query: String,
        maxResults: Int = 40,
        startIndex: Int = 0,
        orderBy: String = "relevance"
    ): Result<List<GoogleBook>>
    suspend fun getBooksBySubject(subject: String): Result<List<GoogleBook>>
    suspend fun getBookDetails(id: String): GoogleBook?
    suspend fun getFavoriteBooks(): List<SavedBookEntity>
    suspend fun getSavedBooks(): List<SavedBookEntity>
    suspend fun toggleFavorite(book: GoogleBook)
    suspend fun toggleSave(book: GoogleBook)
    suspend fun isFavorite(bookId: String): Boolean
    suspend fun isSaved(bookId: String): Boolean
    suspend fun getAiRecommendations(userPreferences: UserPreferences): List<GoogleBook>
    suspend fun getReadBooks(): List<SavedBookEntity>
    suspend fun isRead(bookId: String): Boolean
    suspend fun toggleRead(book: GoogleBook)
}