package com.example.libraryapp.ui.list.model

sealed class UIState {
    object Loading : UIState()
    data class Success(
        val shelves: List<CategoryShelfUi>
    ) : UIState()
    data class Error(val message: String) : UIState()
}