package com.example.libraryapp.ui.airecommend.model

data class UserPreferences(
    val favoriteGenres: List<String> = emptyList(),
    val readingPurpose: String = "",
    val mood: String = ""
)