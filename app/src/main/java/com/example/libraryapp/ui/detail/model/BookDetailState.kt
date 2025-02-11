package com.example.libraryapp.ui.detail.model

import com.example.libraryapp.data.remote.model.GoogleBook

sealed class BookDetailState {
    object Initial : BookDetailState()
    object Loading : BookDetailState()
    data class Success(val book: GoogleBook) : BookDetailState()
    data class Error(val message: String) : BookDetailState()
}