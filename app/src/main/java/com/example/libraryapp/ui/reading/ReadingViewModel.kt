package com.example.libraryapp.ui.reading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ReadingViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    fun saveScrollPosition(bookId: String, scrollY: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.saveLastReadScrollPosition(bookId, scrollY)
        }
    }

    suspend fun getScrollPosition(bookId: String): Int =
        withContext(Dispatchers.IO) {
            bookRepository.getLastReadScrollPosition(bookId)
        }
}
