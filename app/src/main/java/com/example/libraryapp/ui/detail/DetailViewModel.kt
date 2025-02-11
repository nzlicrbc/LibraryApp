package com.example.libraryapp.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.data.remote.model.GoogleBook
import com.example.libraryapp.data.repository.BookRepository
import com.example.libraryapp.ui.detail.model.BookDetailState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _bookState = MutableStateFlow<BookDetailState>(BookDetailState.Initial)
    val bookState: StateFlow<BookDetailState> = _bookState

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    private val _updateProfile = MutableSharedFlow<Unit>()
    val updateProfile = _updateProfile.asSharedFlow()

    private val _isRead = MutableStateFlow(false)
    val isRead: StateFlow<Boolean> = _isRead

    fun getBookWithId(id: String) {
        viewModelScope.launch {
            try {
                _bookState.value = BookDetailState.Loading
                val book = bookRepository.getBookDetails(id)
                if (book != null) {
                    _bookState.value = BookDetailState.Success(book)
                    checkFavoriteStatus(id)
                    checkSaveStatus(id)
                } else {
                    _bookState.value = BookDetailState.Error("Book not found")
                }
            } catch (e: Exception) {
                _bookState.value = BookDetailState.Error(e.message ?: "An error occurred")
            }
        }
    }

    private suspend fun checkFavoriteStatus(bookId: String) {
        _isFavorite.value = bookRepository.isFavorite(bookId)
    }

    private suspend fun checkSaveStatus(bookId: String) {
        _isSaved.value = bookRepository.isSaved(bookId)
    }

    fun toggleFavorite(book: GoogleBook) {
        viewModelScope.launch {
            try {
                bookRepository.toggleFavorite(book)
                _isFavorite.value = !_isFavorite.value
                _updateProfile.emit(Unit)
            } catch (e: Exception) {
                _bookState.value = BookDetailState.Error("An error occurred while adding to favorites")
            }
        }
    }

    fun toggleSave(book: GoogleBook) {
        viewModelScope.launch {
            try {
                bookRepository.toggleSave(book)
                _isSaved.value = !_isSaved.value
                _updateProfile.emit(Unit)
            } catch (e: Exception) {
                _bookState.value = BookDetailState.Error("An error occurred while saving")
            }
        }
    }

    fun toggleRead(book: GoogleBook) {
        viewModelScope.launch {
            bookRepository.toggleRead(book)
            _isRead.value = !_isRead.value
            _updateProfile.emit(Unit)
        }
    }
}