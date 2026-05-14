package com.example.libraryapp.ui.list

import com.example.libraryapp.R
import com.example.libraryapp.data.remote.model.GoogleBook
import com.example.libraryapp.ui.list.model.CategoryShelfUi
import java.util.Locale
import kotlin.math.abs

object BookShelfGrouper {

    private const val OTHERS_KEY = "__others__"

    private val fallbackShelfColors = intArrayOf(
        R.color.shelf_fallback_1,
        R.color.shelf_fallback_2,
        R.color.shelf_fallback_3,
        R.color.shelf_fallback_4,
        R.color.shelf_fallback_5
    )

    fun group(books: List<GoogleBook>, othersTitle: String): List<CategoryShelfUi> {
        val byKey = linkedMapOf<String, MutableList<GoogleBook>>()
        val displayNames = mutableMapOf<String, String>()

        for (book in books) {
            val raw = book.volumeInfo.categories?.firstOrNull()?.trim().orEmpty()
            if (raw.isEmpty()) {
                byKey.getOrPut(OTHERS_KEY) { mutableListOf() }.add(book)
            } else {
                val segment = raw.split("/").first().trim()
                if (segment.isEmpty()) {
                    byKey.getOrPut(OTHERS_KEY) { mutableListOf() }.add(book)
                } else {
                    val key = segment.lowercase(Locale.getDefault())
                    displayNames.putIfAbsent(key, toTitleCase(segment))
                    byKey.getOrPut(key) { mutableListOf() }.add(book)
                }
            }
        }

        val orderedKeys = byKey.keys.filter { it != OTHERS_KEY }.sorted()
        val result = orderedKeys.map { key ->
            val list = byKey[key].orEmpty()
            CategoryShelfUi(
                title = displayNames[key].orEmpty().ifBlank { toTitleCase(key) },
                bookCount = list.size,
                shelfColorRes = shelfColorResForKey(key),
                books = list
            )
        }.toMutableList()

        val others = byKey[OTHERS_KEY]
        if (!others.isNullOrEmpty()) {
            result += CategoryShelfUi(
                title = othersTitle,
                bookCount = others.size,
                shelfColorRes = R.color.shelf_others_base,
                books = others
            )
        }

        return result
    }

    private fun toTitleCase(s: String): String {
        return s.split(" ").joinToString(" ") { word ->
            if (word.isEmpty()) word
            else word.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }
    }

    private fun shelfColorResForKey(key: String): Int {
        return when {
            key.contains("design") || key.contains("graphic") -> R.color.shelf_design
            key.contains("psych") || key.contains("mental") || key.contains("cognitive") ->
                R.color.shelf_psychology
            key.contains("fiction") || key.contains("novel") || key.contains("literary") ->
                R.color.shelf_fiction
            key.contains("fantasy") -> R.color.shelf_fantasy
            key.contains("mystery") || key.contains("thriller") || key.contains("crime") ->
                R.color.shelf_mystery
            key.contains("romance") -> R.color.shelf_romance
            key.contains("science") || key.contains("physics") || key.contains("biology") ->
                R.color.shelf_science
            key.contains("history") || key.contains("historical") -> R.color.shelf_history
            key.contains("biograph") || key.contains("memoir") || key.contains("autobiograph") ->
                R.color.shelf_biography
            key.contains("art") || key.contains("photograph") || key.contains("architecture") ->
                R.color.shelf_art
            key.contains("business") || key.contains("economic") || key.contains("finance") ->
                R.color.shelf_business
            key.contains("comput") || key.contains("programming") || key.contains("software") ->
                R.color.shelf_computers
            key.contains("religion") || key.contains("spiritual") || key.contains("theology") ->
                R.color.shelf_religion
            key.contains("poetry") || key.contains("drama") || key.contains("poems") ->
                R.color.shelf_poetry
            key.contains("juvenile") || key.contains("children") || key.contains("young adult") ->
                R.color.shelf_children
            key.contains("self") && key.contains("help") -> R.color.shelf_self_help
            key.contains("travel") -> R.color.shelf_travel
            key.contains("cook") || key.contains("food") -> R.color.shelf_cooking
            key.contains("health") || key.contains("fitness") -> R.color.shelf_health
            key.contains("philosophy") -> R.color.shelf_philosophy
            else -> fallbackShelfColors[abs(key.hashCode()) % fallbackShelfColors.size]
        }
    }
}
