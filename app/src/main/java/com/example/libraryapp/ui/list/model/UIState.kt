package com.example.libraryapp.ui.list.model

import com.example.libraryapp.data.remote.model.Book
import com.example.libraryapp.data.remote.model.GoogleBook

sealed class UIState {
    object Loading : UIState()
    data class Success(val data: List<GoogleBook>) : UIState()
    data class Error(val message: String) : UIState()
}