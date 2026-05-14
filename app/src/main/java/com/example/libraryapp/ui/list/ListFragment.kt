package com.example.libraryapp.ui.list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.libraryapp.databinding.FragmentListBinding
import com.example.libraryapp.ui.list.adapter.CategoryShelfSectionsAdapter
import com.example.libraryapp.ui.list.model.UIState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListFragment : Fragment() {
    private lateinit var binding: FragmentListBinding
    private lateinit var shelfAdapter: CategoryShelfSectionsAdapter
    private val viewModel: ListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        setupSwipeRefresh()
    }

    private fun setupRecyclerView() {
        shelfAdapter = CategoryShelfSectionsAdapter { bookId ->
            findNavController().navigate(
                ListFragmentDirections.toDetailFragment(bookId)
            )
        }
        binding.recyclerView.adapter = shelfAdapter
        binding.recyclerView.itemAnimator = DefaultItemAnimator().apply {
            addDuration = 240
            removeDuration = 200
            changeDuration = 200
            moveDuration = 280
        }
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return
                if (dy <= 0) return
                val lastVisible = lm.findLastVisibleItemPosition()
                val total = lm.itemCount
                if (total > 0 && lastVisible >= total - 2) {
                    viewModel.loadMore()
                }
            }
        })
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.books.collect { uiState ->
                when (uiState) {
                    is UIState.Loading -> {
                        binding.progressIndicator.isVisible = true
                        binding.loadingMoreProgress.isVisible = false
                    }
                    is UIState.Success -> {
                        binding.progressIndicator.isVisible = false
                        binding.swipeRefreshLayout.isRefreshing = false
                        binding.loadingMoreProgress.isVisible = uiState.loadingMore
                        shelfAdapter.submitList(uiState.shelves)
                    }
                    is UIState.Error -> {
                        binding.progressIndicator.isVisible = false
                        binding.loadingMoreProgress.isVisible = false
                        binding.swipeRefreshLayout.isRefreshing = false
                        Toast.makeText(context, uiState.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
