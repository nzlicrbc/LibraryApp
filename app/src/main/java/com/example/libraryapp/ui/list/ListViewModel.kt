package com.example.libraryapp.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.data.repository.BookRepository
import com.example.libraryapp.ui.list.model.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _books = MutableStateFlow<UIState>(UIState.Loading)
    val books: StateFlow<UIState> = _books

    init {
        loadBooks()
    }

    fun refresh() {
        loadBooks()
    }

    private fun loadBooks() {
        viewModelScope.launch {
            try {
                _books.value = UIState.Loading
                val searchTerms = listOf(
                    "subject:fiction",
                    "subject:fantasy",
                    "subject:mystery",
                    "subject:romance",
                    "subject:science",
                    "subject:history"
                )
                val randomTerm = searchTerms.random()

                val result = bookRepository.searchBooks(
                    query = randomTerm,
                    maxResults = 40,
                    orderBy = "relevance"
                ).shuffled()
                    .take(20)

                _books.value = UIState.Success(result)
            } catch (e: Exception) {
                _books.value = UIState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun getSavedBooksCount(): Int {
        var count = 0
        viewModelScope.launch {
            count = bookRepository.getSavedBooks().size
        }
        return count
    }
}