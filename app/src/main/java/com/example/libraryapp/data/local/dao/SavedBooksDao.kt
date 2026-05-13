package com.example.libraryapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.libraryapp.data.local.entity.SavedBookEntity

@Dao
interface SavedBooksDao {
    @Query("SELECT * FROM saved_books WHERE isFavorite = 1")
    suspend fun getFavoriteBooks(): List<SavedBookEntity>

    @Query("SELECT * FROM saved_books WHERE isSaved = 1")
    suspend fun getSavedBooks(): List<SavedBookEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: SavedBookEntity)

    @Query("SELECT * FROM saved_books WHERE bookId = :bookId")
    suspend fun getBookById(bookId: String): SavedBookEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM saved_books WHERE bookId = :bookId AND isFavorite = 1)")
    suspend fun isFavorite(bookId: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM saved_books WHERE bookId = :bookId AND isSaved = 1)")
    suspend fun isSaved(bookId: String): Boolean

    @Query("UPDATE saved_books SET isFavorite = :isFavorite WHERE bookId = :bookId")
    suspend fun updateFavoriteStatus(bookId: String, isFavorite: Boolean)

    @Query("UPDATE saved_books SET isSaved = :isSaved WHERE bookId = :bookId")
    suspend fun updateSaveStatus(bookId: String, isSaved: Boolean)

    @Query("SELECT * FROM saved_books WHERE isRead = 1")
    suspend fun getReadBooks(): List<SavedBookEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_books WHERE bookId = :bookId AND isRead = 1)")
    suspend fun isRead(bookId: String): Boolean

    @Query("UPDATE saved_books SET isRead = :isRead, completedDate = :completedDate WHERE bookId = :bookId")
    suspend fun updateReadStatus(bookId: String, isRead: Boolean, completedDate: Long? = System.currentTimeMillis())

    @Query("UPDATE saved_books SET lastReadPosition = :position WHERE bookId = :bookId")
    suspend fun updateLastReadPosition(bookId: String, position: Int)
}