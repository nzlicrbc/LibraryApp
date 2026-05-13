package com.example.libraryapp.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.libraryapp.databinding.FragmentSearchBinding
import com.example.libraryapp.ui.list.adapter.BookAdapter
import com.example.libraryapp.ui.search.model.SearchState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var searchAdapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        searchAdapter = BookAdapter(emptyList(), object : BookAdapter.RecyclerViewEvent {
            override fun onItemClick(bookId: String) {
                findNavController().navigate(
                    SearchFragmentDirections.toDetailFragment(bookId)
                )
            }
        })
        binding.searchRecyclerView.adapter = searchAdapter
        binding.loadingMoreBar.isVisible = false
        binding.searchRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return
                if (dy <= 0) return
                val lastVisible = lm.findLastVisibleItemPosition()
                val total = lm.itemCount
                if (total > 0 && lastVisible >= total - 3) {
                    viewModel.loadMore()
                }
            }
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchState.collect { state ->
                    updateUiState(state)
                }
            }
        }
    }

    private fun updateUiState(state: SearchState) {
        when (state) {
            SearchState.Initial -> {
                binding.emptyView.isVisible = false
                binding.progressBar.isVisible = false
                binding.loadingMoreBar.isVisible = false
            }
            SearchState.Loading -> {
                binding.progressBar.isVisible = true
                binding.emptyView.isVisible = false
                binding.loadingMoreBar.isVisible = false
            }
            SearchState.Empty -> {
                binding.progressBar.isVisible = false
                binding.emptyView.isVisible = true
                binding.loadingMoreBar.isVisible = false
                searchAdapter.updateData(emptyList())
            }
            is SearchState.Success -> {
                binding.progressBar.isVisible = false
                binding.emptyView.isVisible = false
                binding.loadingMoreBar.isVisible = state.loadingMore
                searchAdapter.updateData(state.books)
            }
            is SearchState.Error -> {
                binding.progressBar.isVisible = false
                binding.loadingMoreBar.isVisible = false
                context?.let {
                    Toast.makeText(it, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupSearchView() {
        var lastQuery = ""

        binding.searchEditText.doAfterTextChanged { text ->
            val query = text?.toString() ?: ""

            if (query != lastQuery) {
                lastQuery = query
                when {
                    query.isEmpty() -> viewModel.searchBooks("")
                    query.length >= 3 && query.contains(" ") -> viewModel.searchBooks(query.trim())
                    query.length >= 2 && !query.endsWith(" ") -> viewModel.searchBooks(query.trim())
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}