package com.example.libraryapp.ui.list.model

sealed class UIState {
    object Loading : UIState()
    data class Success(
        val shelves: List<CategoryShelfUi>,
        val canLoadMore: Boolean = false,
        val loadingMore: Boolean = false
    ) : UIState()
    data class Error(val message: String) : UIState()
}