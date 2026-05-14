package com.example.libraryapp.ui.list

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.example.libraryapp.R

/**
 * Fixed home shelves: stable order, each backed by a Google Books subject query.
 */
object HomeShelfCatalog {

    data class Definition(
        val id: String,
        /** Subject term for `subject:` search. */
        val subject: String,
        @StringRes val titleRes: Int,
        @ColorRes val shelfColorRes: Int
    )

    val items: List<Definition> = listOf(
        Definition("fiction", "fiction", R.string.shelf_title_fiction, R.color.shelf_fiction),
        Definition("design", "design", R.string.shelf_title_design, R.color.shelf_design),
        Definition("psychology", "psychology", R.string.shelf_title_psychology, R.color.shelf_psychology),
        Definition("history", "history", R.string.shelf_title_history, R.color.shelf_history),
        Definition("science", "science", R.string.shelf_title_science, R.color.shelf_science),
        Definition("romance", "romance", R.string.shelf_title_romance, R.color.shelf_romance),
        Definition("biography", "biography", R.string.shelf_title_biography, R.color.shelf_biography),
        Definition("art", "art", R.string.shelf_title_art, R.color.shelf_art),
        Definition("business", "business", R.string.shelf_title_business, R.color.shelf_business),
        Definition("fantasy", "fantasy", R.string.shelf_title_fantasy, R.color.shelf_fantasy),
        Definition("mystery", "mystery", R.string.shelf_title_mystery, R.color.shelf_mystery),
        Definition("poetry", "poetry", R.string.shelf_title_poetry, R.color.shelf_poetry),
        Definition("religion", "religion", R.string.shelf_title_religion, R.color.shelf_religion)
    )
}
