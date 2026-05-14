package com.example.libraryapp.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.data.remote.model.GoogleBook
import com.example.libraryapp.data.repository.BookRepository
import com.example.libraryapp.data.repository.BookPage
import com.example.libraryapp.ui.list.model.CategoryShelfUi
import com.example.libraryapp.ui.list.model.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
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

    private val initialPageSize = 20
    private val loadMorePageSize = 20

    private var shelfBooks: MutableMap<String, MutableList<GoogleBook>> =
        emptyShelfBooks()
    private var shelfStartIndex: MutableMap<String, Int> = emptyStartIndices()
    private var shelfHasMore: MutableMap<String, Boolean> = emptyHasMoreFlags()
    private var shelfUseKeywordSearch: MutableMap<String, Boolean> =
        HomeShelfCatalog.items.associate { it.id to false }.toMutableMap()
    /** Exact `q` string for keyword-mode pagination (includes fallback queries). */
    private var shelfSearchQuery: MutableMap<String, String> =
        HomeShelfCatalog.items.associate { it.id to it.subject }.toMutableMap()
    private val shelvesLoading = mutableSetOf<String>()

    /** Invalidates in-flight shelf loads after refresh so races cannot mix old/new data. */
    private var loadGeneration = 0
    private var fullReloadJob: Job? = null

    init {
        loadBooks()
    }

    fun refresh() {
        loadBooks()
    }

    fun loadMoreForShelf(shelfId: String) {
        val state = _books.value
        if (state !is UIState.Success) return
        val def = HomeShelfCatalog.items.find { it.id == shelfId } ?: return
        if (shelfId in shelvesLoading) return
        if (shelfHasMore[shelfId] != true) return

        val gen = loadGeneration
        viewModelScope.launch {
            if (gen != loadGeneration) return@launch
            shelvesLoading.add(shelfId)
            _books.value = UIState.Success(
                state.shelves.map {
                    it.copy(
                        isLoadingMore = it.shelfId == shelfId,
                        resetHorizontalScroll = false
                    )
                }
            )

            val start = shelfStartIndex[shelfId] ?: 0
            val result = fetchShelfPage(def, start, loadMorePageSize)
            result.fold(
                onSuccess = { page ->
                    if (gen != loadGeneration) return@launch
                    shelvesLoading.remove(shelfId)
                    val list = shelfBooks.getOrPut(shelfId) { mutableListOf() }
                    for (book in page.items) {
                        if (list.none { it.id == book.id }) list.add(book)
                    }
                    shelfStartIndex[shelfId] = start + page.items.size
                    shelfHasMore[shelfId] = page.hasMore
                    emitShelfSuccess(resetHorizontalScroll = false)
                },
                onFailure = {
                    if (gen != loadGeneration) return@launch
                    shelvesLoading.remove(shelfId)
                    emitShelfSuccess(resetHorizontalScroll = false)
                }
            )
        }
    }

    private fun loadBooks() {
        fullReloadJob?.cancel()
        loadGeneration++
        val gen = loadGeneration
        fullReloadJob = viewModelScope.launch {
            _books.value = UIState.Loading
            shelfBooks = emptyShelfBooks()
            shelfStartIndex = emptyStartIndices()
            shelfHasMore = emptyHasMoreFlags()
            shelfUseKeywordSearch = HomeShelfCatalog.items.associate { it.id to false }.toMutableMap()
            shelfSearchQuery = HomeShelfCatalog.items.associate { it.id to it.subject }.toMutableMap()
            shelvesLoading.clear()

            val outcome = fetchAllShelvesInitial(gen)
            if (gen != loadGeneration) return@launch
            outcome.fold(
                onSuccess = { emitShelfSuccess(resetHorizontalScroll = true) },
                onFailure = { e ->
                    if (e is CancellationException) throw e
                    _books.value = UIState.Error(e.message ?: "An error occurred")
                }
            )
        }
    }

    private suspend fun fetchAllShelvesInitial(expectedGeneration: Int): Result<Unit> {
        val pairResults: List<Pair<String, Result<ShelfFirstFetch>>> = coroutineScope {
            HomeShelfCatalog.items.map { def ->
                async { def.id to fetchShelfFirstPage(def) }
            }.awaitAll()
        }
        if (expectedGeneration != loadGeneration) {
            return Result.failure(CancellationException())
        }

        val anySuccess = pairResults.any { (_, r) -> r.isSuccess }
        if (!anySuccess) {
            val err = pairResults.firstOrNull { (_, r) -> r.isFailure }?.second?.exceptionOrNull()
            return Result.failure(err ?: Exception("An error occurred"))
        }

        val newBooks = emptyShelfBooks()
        val newStart = emptyStartIndices()
        val newHasMore = emptyHasMoreFlags()
        val newKeyword = HomeShelfCatalog.items.associate { it.id to false }.toMutableMap()
        val newQueries = HomeShelfCatalog.items.associate { it.id to it.subject }.toMutableMap()

        for ((shelfId, result) in pairResults) {
            result.fold(
                onSuccess = { fetch ->
                    newBooks[shelfId]?.clear()
                    newBooks[shelfId]?.addAll(fetch.page.items)
                    newStart[shelfId] = fetch.page.items.size
                    newHasMore[shelfId] = fetch.page.hasMore
                    newKeyword[shelfId] = fetch.useKeywordSearch
                    newQueries[shelfId] = HomeShelfCatalog.items.first { it.id == shelfId }.subject
                },
                onFailure = {
                    newBooks[shelfId]?.clear()
                    newStart[shelfId] = 0
                    newHasMore[shelfId] = false
                    newKeyword[shelfId] = false
                }
            )
        }

        for (def in HomeShelfCatalog.items) {
            if (newBooks[def.id].isNullOrEmpty()) {
                fillEmptyShelfWithFallbacks(
                    def = def,
                    newBooks = newBooks,
                    newStart = newStart,
                    newHasMore = newHasMore,
                    newKeyword = newKeyword,
                    newQueries = newQueries
                )
            }
        }

        if (expectedGeneration != loadGeneration) {
            return Result.failure(CancellationException())
        }

        shelfBooks = newBooks
        shelfStartIndex = newStart
        shelfHasMore = newHasMore
        shelfUseKeywordSearch = newKeyword
        shelfSearchQuery = newQueries
        return Result.success(Unit)
    }

    private suspend fun fillEmptyShelfWithFallbacks(
        def: HomeShelfCatalog.Definition,
        newBooks: MutableMap<String, MutableList<GoogleBook>>,
        newStart: MutableMap<String, Int>,
        newHasMore: MutableMap<String, Boolean>,
        newKeyword: MutableMap<String, Boolean>,
        newQueries: MutableMap<String, String>
    ) {
        val queries = listOf(
            "subject:\"${def.subject}\"",
            "${def.subject} books",
            def.subject.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            "${def.subject} literature"
        ).distinct()

        for (q in queries) {
            val r = bookRepository.searchBooks(
                query = q,
                maxResults = initialPageSize,
                startIndex = 0
            )
            val page = r.getOrNull() ?: continue
            if (page.items.isEmpty()) continue
            newBooks[def.id]?.clear()
            newBooks[def.id]?.addAll(page.items)
            newStart[def.id] = page.items.size
            newHasMore[def.id] = page.hasMore
            newKeyword[def.id] = true
            newQueries[def.id] = q
            return
        }
    }

    private suspend fun fetchShelfFirstPage(def: HomeShelfCatalog.Definition): Result<ShelfFirstFetch> {
        val staggerIndex = HomeShelfCatalog.items.indexOfFirst { it.id == def.id }
        if (staggerIndex >= 0) delay(staggerIndex * 40L)
        val subjectResult = bookRepository.getBooksBySubject(
            subject = def.subject,
            maxResults = initialPageSize,
            startIndex = 0
        )
        subjectResult.getOrNull()?.let { page ->
            if (page.items.isNotEmpty()) {
                return Result.success(ShelfFirstFetch(page, false))
            }
        }
        val keywordResult = bookRepository.searchBooks(
            query = def.subject,
            maxResults = initialPageSize,
            startIndex = 0
        )
        keywordResult.getOrNull()?.let { page ->
            if (page.items.isNotEmpty()) {
                return Result.success(ShelfFirstFetch(page, true))
            }
        }
        val subjectFailed = subjectResult.isFailure
        val lastPage = keywordResult.getOrNull()
            ?: subjectResult.getOrNull()
            ?: BookPage(emptyList(), false)
        if (subjectFailed && keywordResult.isFailure) {
            return Result.failure(
                keywordResult.exceptionOrNull()
                    ?: subjectResult.exceptionOrNull()
                    ?: Exception("Network error")
            )
        }
        return Result.success(ShelfFirstFetch(lastPage, true))
    }

    private suspend fun fetchShelfPage(
        def: HomeShelfCatalog.Definition,
        startIndex: Int,
        maxResults: Int
    ): Result<BookPage> {
        return if (shelfUseKeywordSearch[def.id] == true) {
            val q = shelfSearchQuery[def.id] ?: def.subject
            bookRepository.searchBooks(q, maxResults, startIndex)
        } else {
            bookRepository.getBooksBySubject(def.subject, maxResults, startIndex)
        }
    }

    private fun emitShelfSuccess(resetHorizontalScroll: Boolean = false) {
        _books.value = UIState.Success(buildShelfUiList(resetHorizontalScroll))
    }

    private fun buildShelfUiList(resetHorizontalScroll: Boolean): List<CategoryShelfUi> =
        HomeShelfCatalog.items.map { def ->
            val books = shelfBooks[def.id].orEmpty().toList()
            CategoryShelfUi(
                shelfId = def.id,
                titleRes = def.titleRes,
                bookCount = books.size,
                shelfColorRes = def.shelfColorRes,
                books = books,
                isLoadingMore = def.id in shelvesLoading,
                canLoadMore = shelfHasMore[def.id] == true,
                resetHorizontalScroll = resetHorizontalScroll
            )
        }

    private fun emptyShelfBooks() =
        HomeShelfCatalog.items.associate { it.id to mutableListOf<GoogleBook>() }.toMutableMap()

    private fun emptyStartIndices() =
        HomeShelfCatalog.items.associate { it.id to 0 }.toMutableMap()

    private fun emptyHasMoreFlags() =
        HomeShelfCatalog.items.associate { it.id to false }.toMutableMap()

    private data class ShelfFirstFetch(
        val page: BookPage,
        val useKeywordSearch: Boolean
    )
}
