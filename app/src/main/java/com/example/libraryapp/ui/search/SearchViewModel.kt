package com.example.libraryapp.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun searchBooks(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                if (query.isEmpty()) {
                    _searchState.value = SearchState.Initial
                    return@launch
                }

                _searchState.value = SearchState.Loading
                delay(300)

                val results = withTimeout(30000L) {
                    when {
                        query.length >= 3 && query.contains(" ") -> {
                            bookRepository.searchBooks(
                                query = "intitle:\"$query\"",
                                maxResults = 5
                            ).distinctBy { it.volumeInfo.title }
                        }
                        query.length >= 2 -> {
                            bookRepository.searchBooks(
                                query = "intitle:$query",
                                maxResults = 10
                            ).distinctBy { it.volumeInfo.title }
                                .sortedBy { it.volumeInfo.title?.length }
                        }
                        else -> emptyList()
                    }
                }

                if (results.isEmpty()) {
                    _searchState.value = SearchState.Empty
                } else {
                    _searchState.value = SearchState.Success(results)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _searchState.value = SearchState.Error(e.message ?: "An error occurred")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}