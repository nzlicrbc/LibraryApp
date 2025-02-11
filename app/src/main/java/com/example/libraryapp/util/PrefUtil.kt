package com.example.libraryapp.util

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PrefUtil {

    private lateinit var pref: SharedPreferences
    private lateinit var gson: Gson

    private const val PREF_NAME = "Library App"
    private const val SCROLL_POSITION_PREFIX = "scrollPosition_"
    private const val BOOK_IDS_KEY = "bookIds"

    private val editor: Editor
        get() = pref.edit()

    fun initPref(context: Context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        gson = Gson()
    }

    private fun getBookIds(): List<Int> {
        val bookIdsJson = pref.getString(BOOK_IDS_KEY, null)

        return if (bookIdsJson != null) {
            gson.fromJson(bookIdsJson, object : TypeToken<List<Int>>() {}.type)
        } else {
            emptyList()
        }
    }

    fun saveBookId(bookId: Int) {
        val existingBookIds = getBookIds().toMutableList()

        if(!existingBookIds.contains(bookId)){
            existingBookIds.add(bookId)

            val bookIdsJson = gson.toJson(existingBookIds)
            editor.putString(this.BOOK_IDS_KEY, bookIdsJson).apply()
        }
    }

    fun checkBook(id: Int): Boolean {
        val bookIdsJson = pref.getString(this.BOOK_IDS_KEY, null)
        if (bookIdsJson != null) {
            val listType = object : TypeToken<List<Int>>() {}.type
            val bookIds = gson.fromJson<List<Int>>(bookIdsJson, listType)
            return bookIds.contains(id)
        }
        return false
    }

    fun getSavedBooksCount(): Int {
        val bookIdsJson = pref.getString(this.BOOK_IDS_KEY, null) ?: return  0
        val listType = object : TypeToken<List<Int>>() {}.type
        val bookIds = gson.fromJson<List<Int>>(bookIdsJson, listType)
        return bookIds.size
    }

    fun saveBookScrollPosition(bookId: String, scrollPosition: Int) {
        editor.putInt(SCROLL_POSITION_PREFIX + bookId, scrollPosition).apply()
    }

    fun getBookScrollPosition(bookId: String): Int {
        return pref.getInt(SCROLL_POSITION_PREFIX + bookId, 0)
    }
}