package com.example.libraryapp.data.repository

import com.example.libraryapp.data.remote.model.GoogleBook

data class BookPage(
    val items: List<GoogleBook>,
    val hasMore: Boolean
)
