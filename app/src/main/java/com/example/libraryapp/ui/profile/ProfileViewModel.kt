package com.example.libraryapp.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.data.mapper.toGoogleBook
import com.example.libraryapp.data.remote.model.GoogleBook
import com.example.libraryapp.data.repository.BookRepository
import com.example.libraryapp.ui.profile.model.ProfileStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _favoriteBooks = MutableStateFlow<List<GoogleBook>>(emptyList())
    val favoriteBooks: MutableStateFlow<List<GoogleBook>> = _favoriteBooks

    private val _savedBooks = MutableStateFlow<List<GoogleBook>>(emptyList())
    val savedBooks: MutableStateFlow<List<GoogleBook>> = _savedBooks

    private val _readBooks = MutableStateFlow<List<GoogleBook>>(emptyList())
    val readBooks: StateFlow<List<GoogleBook>> = _readBooks

    private val _stats = MutableStateFlow(ProfileStats(0, 0, 0))
    val stats: StateFlow<ProfileStats> = _stats

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val savedBooks = bookRepository.getSavedBooks()
                val favoriteBooks = bookRepository.getFavoriteBooks()
                val readBooks = bookRepository.getReadBooks()

                _savedBooks.value = savedBooks.map { it.toGoogleBook() }
                _favoriteBooks.value = favoriteBooks.map { it.toGoogleBook() }
                _readBooks.value = readBooks.map { it.toGoogleBook() }

                _stats.value = ProfileStats(
                    savedCount = savedBooks.size,
                    favoriteCount = favoriteBooks.size,
                    readCount = readBooks.size
                )
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading books", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}