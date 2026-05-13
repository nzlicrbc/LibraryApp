package com.example.libraryapp.data.repository

import android.util.Log
import com.example.libraryapp.data.local.dao.SavedBooksDao
import com.example.libraryapp.data.local.entity.SavedBookEntity
import com.example.libraryapp.data.remote.GeminiService
import com.example.libraryapp.data.remote.GoogleBooksApi
import com.example.libraryapp.data.remote.model.GoogleBook
import com.example.libraryapp.ui.airecommend.model.UserPreferences
import com.example.libraryapp.util.PrefUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class BookRepositoryImpl @Inject constructor(
    private val googleBooksApi: GoogleBooksApi,
    private val savedBooksDao: SavedBooksDao,
    private val geminiService: GeminiService
) : BookRepository {

    override suspend fun searchBooks(
        query: String,
        maxResults: Int,
        startIndex: Int,
        orderBy: String
    ): Result<BookPage> {
        return withContext(Dispatchers.IO) {
            try {
                ensureActive()

                val response = googleBooksApi.searchBooks(
                    query = query,
                    maxResults = maxResults,
                    startIndex = startIndex,
                    orderBy = orderBy
                )

                ensureActive()
                val items = response.items ?: emptyList()
                val hasMore = items.isNotEmpty() &&
                    startIndex + items.size < response.totalItems
                Result.success(BookPage(items, hasMore))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("BookRepository", "Error searching books", e)
                Result.failure(e)
            }
        }
    }

    override suspend fun getBooksBySubject(
        subject: String,
        maxResults: Int,
        startIndex: Int
    ): Result<BookPage> {
        return withContext(Dispatchers.IO) {
            try {
                ensureActive()
                val response = googleBooksApi.searchBooks(
                    query = "subject:$subject",
                    maxResults = maxResults,
                    startIndex = startIndex
                )
                val items = response.items ?: emptyList()
                val hasMore = items.isNotEmpty() &&
                    startIndex + items.size < response.totalItems
                Result.success(BookPage(items, hasMore))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("BookRepository", "Error getting books by subject", e)
                Result.failure(e)
            }
        }
    }

    override suspend fun getBookDetails(id: String): GoogleBook? {
        return try {
            googleBooksApi.getBookDetails(id)
        } catch (e: Exception) {
            Log.e("BookRepository", "Error getting book details", e)
            null
        }
    }

    override suspend fun toggleFavorite(book: GoogleBook) {
        try {
            val savedBook = savedBooksDao.getBookById(book.id)

            if (savedBook != null) {
                Log.d("BookRepository", "Updating favorite status for book ${book.id}")
                savedBooksDao.updateFavoriteStatus(book.id, !savedBook.isFavorite)
            } else {
                Log.d("BookRepository", "Inserting new favorite book ${book.id}")
                savedBooksDao.insertBook(
                    SavedBookEntity(
                        bookId = book.id,
                        title = book.volumeInfo.title ?: "",
                        author = book.volumeInfo.authors?.firstOrNull() ?: "",
                        thumbnailUrl = book.volumeInfo.imageLinks?.thumbnail,
                        isFavorite = true,
                        isSaved = false,
                        lastReadPosition = PrefUtil.getBookScrollPosition(book.id)
                    )
                )
                PrefUtil.clearBookScrollPosition(book.id)
            }
        } catch (e: Exception) {
            Log.e("BookRepository", "Error toggling favorite", e)
        }
    }

    override suspend fun toggleSave(book: GoogleBook) {
        try {
            val savedBook = savedBooksDao.getBookById(book.id)

            if (savedBook != null) {
                Log.d("BookRepository", "Updating save status for book ${book.id}")
                savedBooksDao.updateSaveStatus(book.id, !savedBook.isSaved)
            } else {
                Log.d("BookRepository", "Inserting new saved book ${book.id}")
                savedBooksDao.insertBook(
                    SavedBookEntity(
                        bookId = book.id,
                        title = book.volumeInfo.title ?: "",
                        author = book.volumeInfo.authors?.firstOrNull() ?: "",
                        thumbnailUrl = book.volumeInfo.imageLinks?.thumbnail,
                        isFavorite = false,
                        isSaved = true,
                        lastReadPosition = PrefUtil.getBookScrollPosition(book.id)
                    )
                )
                PrefUtil.clearBookScrollPosition(book.id)
            }
        } catch (e: Exception) {
            Log.e("BookRepository", "Error toggling save", e)
        }
    }

    override suspend fun getAiRecommendations(userPreferences: UserPreferences): List<GoogleBook> {
        return withContext(Dispatchers.IO) {
            try {
                val results = mutableListOf<GoogleBook>()

                userPreferences.favoriteGenres.forEach { genre ->
                    val query = when (genre) {
                        "Roman" -> "subject:fiction"
                        "Bilim Kurgu" -> "subject:\"science fiction\""
                        "Polisiye" -> "subject:mystery"
                        "Kişisel Gelişim" -> "subject:\"self-help\""
                        "Fantastik" -> "subject:fantasy"
                        "Tarih" -> "subject:history"
                        "Biyografi" -> "subject:biography"
                        else -> "subject:${genre.lowercase()}"
                    }

                    try {
                        val response = googleBooksApi.searchBooks(
                            query = query,
                            maxResults = 10
                        )

                        response.items?.let { books ->
                            results.addAll(books.shuffled().take(2))
                        }
                    } catch (e: Exception) {
                        Log.e("BookRepository", "Error searching for genre: $genre", e)
                    }
                }

                results.shuffled().distinctBy { it.id }.take(5)
            } catch (e: Exception) {
                Log.e("BookRepository", "Error in recommendations", e)
                emptyList()
            }
        }
    }

    override suspend fun isFavorite(bookId: String): Boolean {
        return try {
            savedBooksDao.isFavorite(bookId)
        } catch (e: Exception) {
            Log.e("BookRepository", "Error checking favorite status", e)
            false
        }
    }

    override suspend fun isSaved(bookId: String): Boolean {
        return try {
            savedBooksDao.isSaved(bookId)
        } catch (e: Exception) {
            Log.e("BookRepository", "Error checking save status", e)
            false
        }
    }

    override suspend fun getFavoriteBooks(): List<SavedBookEntity> {
        return try {
            val books = savedBooksDao.getFavoriteBooks()
            Log.d("BookRepository", "Retrieved ${books.size} favorite books")
            books
        } catch (e: Exception) {
            Log.e("BookRepository", "Error getting favorite books", e)
            emptyList()
        }
    }

    override suspend fun getSavedBooks(): List<SavedBookEntity> {
        return try {
            val books = savedBooksDao.getSavedBooks()
            Log.d("BookRepository", "Retrieved ${books.size} saved books")
            books
        } catch (e: Exception) {
            Log.e("BookRepository", "Error getting saved books", e)
            emptyList()
        }
    }

    override suspend fun getReadBooks(): List<SavedBookEntity> {
        return try {
            savedBooksDao.getReadBooks()
        } catch (e: Exception) {
            Log.e("BookRepository", "Error getting read books", e)
            emptyList()
        }
    }

    override suspend fun isRead(bookId: String): Boolean {
        return try {
            savedBooksDao.isRead(bookId)
        } catch (e: Exception) {
            Log.e("BookRepository", "Error checking read status", e)
            false
        }
    }

    override suspend fun toggleRead(book: GoogleBook) {
        try {
            val savedBook = savedBooksDao.getBookById(book.id)

            if (savedBook != null) {
                savedBooksDao.updateReadStatus(book.id, !savedBook.isRead)
            } else {
                savedBooksDao.insertBook(
                    SavedBookEntity(
                        bookId = book.id,
                        title = book.volumeInfo.title ?: "",
                        author = book.volumeInfo.authors?.firstOrNull() ?: "",
                        thumbnailUrl = book.volumeInfo.imageLinks?.thumbnail,
                        isRead = true,
                        completedDate = System.currentTimeMillis(),
                        lastReadPosition = PrefUtil.getBookScrollPosition(book.id)
                    )
                )
                PrefUtil.clearBookScrollPosition(book.id)
            }
        } catch (e: Exception) {
            Log.e("BookRepository", "Error toggling read status", e)
        }
    }

    override suspend fun getLastReadScrollPosition(bookId: String): Int {
        return withContext(Dispatchers.IO) {
            val entity = savedBooksDao.getBookById(bookId)
            val pref = PrefUtil.getBookScrollPosition(bookId)
            val db = entity?.lastReadPosition ?: 0
            val merged = maxOf(db, pref)
            if (entity != null && merged > db) {
                savedBooksDao.updateLastReadPosition(bookId, merged)
            }
            merged
        }
    }

    override suspend fun saveLastReadScrollPosition(bookId: String, position: Int) {
        withContext(Dispatchers.IO) {
            if (savedBooksDao.getBookById(bookId) != null) {
                savedBooksDao.updateLastReadPosition(bookId, position)
            } else {
                PrefUtil.saveBookScrollPosition(bookId, position)
            }
        }
    }
}