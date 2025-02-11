package com.example.libraryapp.ui.search.model

import com.example.libraryapp.data.remote.model.GoogleBook


sealed class SearchState {
    object Initial : SearchState()
    object Loading : SearchState()
    object Empty : SearchState()
    data class Success(val books: List<GoogleBook>) : SearchState()
    data class Error(val message: String) : SearchState()
}