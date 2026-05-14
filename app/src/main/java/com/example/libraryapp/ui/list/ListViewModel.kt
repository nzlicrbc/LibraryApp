package com.example.libraryapp.ui.list

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.R
import com.example.libraryapp.data.remote.model.GoogleBook
import com.example.libraryapp.data.repository.BookRepository
import com.example.libraryapp.ui.list.model.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _books = MutableStateFlow<UIState>(UIState.Loading)
    val books: StateFlow<UIState> = _books

    private val subjects = listOf(
        "fiction",
        "design",
        "psychology",
        "history",
        "science",
        "romance",
        "biography",
        "art",
        "business",
        "fantasy",
        "mystery",
        "poetry",
        "religion"
    )

    private val initialBatchPerSubject = 8
    private val pageSizeLoadMore = 16

    private var subjectOffsets: MutableMap<String, Int> =
        subjects.associateWith { 0 }.toMutableMap()

    private var subjectHasMore: MutableMap<String, Boolean> =
        subjects.associateWith { false }.toMutableMap()

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

        viewModelScope.launch {
            _books.value = state.copy(loadingMore = true)
            val subject = subjects.random()
            val start = subjectOffsets[subject] ?: 0

            bookRepository.getBooksBySubject(
                subject = subject,
                maxResults = pageSizeLoadMore,
                startIndex = start
            ).fold(
                onSuccess = { page ->
                    subjectOffsets[subject] = start + page.items.size
                    subjectHasMore[subject] = page.hasMore
                    accumulated = (accumulated + page.items).distinctBy { it.id }
                    val canMore = subjectHasMore.values.any { it }
                    _books.value = UIState.Success(
                        shelves = shelvesFromAccumulated(),
                        canLoadMore = canMore,
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
            subjectOffsets = subjects.associateWith { 0 }.toMutableMap()
            subjectHasMore = subjects.associateWith { false }.toMutableMap()
            accumulated = emptyList()

            fetchMergedInitial().fold(
                onSuccess = { merge ->
                    accumulated = merge.books.shuffled()
                    merge.offsetUpdates.forEach { (subject, newOffset) ->
                        subjectOffsets[subject] = newOffset
                    }
                    merge.hasMoreBySubject.forEach { (subject, hasMore) ->
                        subjectHasMore[subject] = hasMore
                    }
                    val anyHasMore = subjectHasMore.values.any { it }
                    _books.value = UIState.Success(
                        shelves = shelvesFromAccumulated(),
                        canLoadMore = anyHasMore,
                        loadingMore = false
                    )
                },
                onFailure = { e ->
                    _books.value = UIState.Error(e.message ?: "An error occurred")
                }
            )
        }
    }

    private suspend fun fetchMergedInitial(): Result<InitialMergeResult> = coroutineScope {
        val deferred = subjects.map { subject ->
            async {
                bookRepository.getBooksBySubject(
                    subject = subject,
                    maxResults = initialBatchPerSubject,
                    startIndex = 0
                )
            }
        }
        val results = deferred.awaitAll()
        val successes = results.mapNotNull { it.getOrNull() }
        if (successes.isEmpty()) {
            val err = results.firstOrNull { it.isFailure }?.exceptionOrNull()
            return@coroutineScope Result.failure(err ?: Exception("An error occurred"))
        }

        val books = successes.flatMap { it.items }.distinctBy { it.id }
        val offsetUpdates = buildMap {
            subjects.forEachIndexed { index, subject ->
                results.getOrNull(index)?.getOrNull()?.let { page ->
                    put(subject, page.items.size)
                }
            }
        }
        val hasMoreBySubject = buildMap {
            subjects.forEachIndexed { index, subject ->
                results.getOrNull(index)?.getOrNull()?.let { page ->
                    put(subject, page.hasMore)
                }
            }
        }

        Result.success(
            InitialMergeResult(
                books = books,
                offsetUpdates = offsetUpdates,
                hasMoreBySubject = hasMoreBySubject
            )
        )
    }

    private fun shelvesFromAccumulated() = BookShelfGrouper.group(
        accumulated,
        appContext.getString(R.string.category_others)
    )

    private data class InitialMergeResult(
        val books: List<GoogleBook>,
        val offsetUpdates: Map<String, Int>,
        val hasMoreBySubject: Map<String, Boolean>
    )
}
