package com.example.libraryapp.ui.list.model

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.example.libraryapp.data.remote.model.GoogleBook

data class CategoryShelfUi(
    val shelfId: String,
    @StringRes val titleRes: Int,
    val bookCount: Int,
    @ColorRes val shelfColorRes: Int,
    val books: List<GoogleBook>,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = false
)
