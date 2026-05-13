package com.example.libraryapp.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.data.remote.model.GoogleBook
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

    private val subjects = listOf(
        "fiction",
        "fantasy",
        "mystery",
        "romance",
        "science",
        "history"
    )
    private val pageSize = 20
    private var currentSubject = ""
    private var nextStartIndex = 0
    private var accumulated = listOf<GoogleBook>()

    init {
        loadBooks()
    }

    fun refresh() {
        loadBooks()
    }

    fun loadMore() {
        val state = _books.value
        if (state !is UIState.Success || !state.canLoadMore || state.loadingMore) return
        if (currentSubject.isEmpty()) return

        viewModelScope.launch {
            _books.value = state.copy(loadingMore = true)
            bookRepository.getBooksBySubject(
                subject = currentSubject,
                maxResults = pageSize,
                startIndex = nextStartIndex
            ).fold(
                onSuccess = { page ->
                    val merged = (accumulated + page.items).distinctBy { it.id }
                    accumulated = merged
                    nextStartIndex += page.items.size
                    _books.value = UIState.Success(
                        data = merged,
                        canLoadMore = page.hasMore,
                        loadingMore = false
                    )
                },
                onFailure = {
                    _books.value = state.copy(loadingMore = false)
                }
            )
        }
    }

    private fun loadBooks() {
        viewModelScope.launch {
            _books.value = UIState.Loading
            currentSubject = subjects.random()
            nextStartIndex = 0
            accumulated = emptyList()

            bookRepository.getBooksBySubject(
                subject = currentSubject,
                maxResults = pageSize,
                startIndex = 0
            ).fold(
                onSuccess = { page ->
                    val first = page.items.shuffled()
                    accumulated = first
                    nextStartIndex = page.items.size
                    _books.value = UIState.Success(
                        data = first,
                        canLoadMore = page.hasMore,
                        loadingMore = false
                    )
                },
                onFailure = { e ->
                    _books.value = UIState.Error(e.message ?: "An error occurred")
                }
            )
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
