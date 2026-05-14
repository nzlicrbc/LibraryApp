package com.example.libraryapp.ui.list.model

import androidx.annotation.ColorRes
import com.example.libraryapp.data.remote.model.GoogleBook

data class CategoryShelfUi(
    val title: String,
    val bookCount: Int,
    @ColorRes val shelfColorRes: Int,
    val books: List<GoogleBook>
)
