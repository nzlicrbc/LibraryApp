package com.example.libraryapp.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.data.remote.model.GoogleBook
import com.example.libraryapp.data.repository.BookRepository
import com.example.libraryapp.ui.search.model.SearchState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Initial)
    val searchState: StateFlow<SearchState> = _searchState

    private var searchJob: Job? = null

    private var accumulated = listOf<GoogleBook>()
    private var nextStartIndex = 0
    private var activeApiQuery: String? = null
    private var maxResults = 10
    private var phraseMode = false

    fun searchBooks(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                if (query.isEmpty()) {
                    accumulated = emptyList()
                    nextStartIndex = 0
                    activeApiQuery = null
                    _searchState.value = SearchState.Initial
                    return@launch
                }

                if (query.length < 2) {
                    _searchState.value = SearchState.Initial
                    return@launch
                }

                val newPhraseMode = query.length >= 3 && query.contains(" ")
                val newMax = if (newPhraseMode) 5 else 10
                val apiQuery = if (newPhraseMode) "intitle:\"$query\"" else "intitle:$query"

                if (apiQuery != activeApiQuery) {
                    accumulated = emptyList()
                    nextStartIndex = 0
                    activeApiQuery = apiQuery
                    phraseMode = newPhraseMode
                    maxResults = newMax
                }

                _searchState.value = SearchState.Loading
                delay(300)

                val outcome = withTimeout(30000L) {
                    bookRepository.searchBooks(
                        query = apiQuery,
                        maxResults = maxResults,
                        startIndex = nextStartIndex
                    )
                }

                outcome.fold(
                    onSuccess = { page ->
                        val slice = if (phraseMode) {
                            page.items.distinctBy { it.volumeInfo.title }
                        } else {
                            page.items.distinctBy { it.volumeInfo.title }
                                .sortedBy { it.volumeInfo.title?.length }
                        }
                        accumulated = (accumulated + slice).distinctBy { it.id }
                        nextStartIndex += page.items.size
                        if (accumulated.isEmpty()) {
                            _searchState.value = SearchState.Empty
                        } else {
                            _searchState.value = SearchState.Success(
                                books = accumulated,
                                canLoadMore = page.hasMore,
                                loadingMore = false
                            )
                        }
                    },
                    onFailure = { e ->
                        _searchState.value = SearchState.Error(e.message ?: "An error occurred")
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _searchState.value = SearchState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun loadMore() {
        val state = _searchState.value
        if (state !is SearchState.Success) return
        if (!state.canLoadMore || state.loadingMore) return
        val q = activeApiQuery ?: return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                _searchState.value = state.copy(loadingMore = true)
                val outcome = withTimeout(30000L) {
                    bookRepository.searchBooks(
                        query = q,
                        maxResults = maxResults,
                        startIndex = nextStartIndex
                    )
                }
                outcome.fold(
                    onSuccess = { page ->
                        val slice = if (phraseMode) {
                            page.items.distinctBy { it.volumeInfo.title }
                        } else {
                            page.items.distinctBy { it.volumeInfo.title }
                                .sortedBy { it.volumeInfo.title?.length }
                        }
                        accumulated = (accumulated + slice).distinctBy { it.id }
                        nextStartIndex += page.items.size
                        _searchState.value = SearchState.Success(
                            books = accumulated,
                            canLoadMore = page.hasMore,
                            loadingMore = false
                        )
                    },
                    onFailure = {
                        _searchState.value = state.copy(loadingMore = false)
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _searchState.value = state.copy(loadingMore = false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}
