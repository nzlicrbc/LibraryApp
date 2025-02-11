package com.example.libraryapp.ui.profile

import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReadBooksFragment : BaseBookListFragment() {
    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.readBooks.collect { books ->
                bookAdapter.updateData(books)
                binding.emptyView.isVisible = books.isEmpty()
            }
        }
    }
}