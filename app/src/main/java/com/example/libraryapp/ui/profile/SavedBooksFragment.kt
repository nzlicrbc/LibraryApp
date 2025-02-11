package com.example.libraryapp.ui.profile

import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SavedBooksFragment : BaseBookListFragment() {
    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.savedBooks.collect { books ->
                bookAdapter.updateData(books)
                binding.emptyView.isVisible = books.isEmpty()
            }
        }
    }
}