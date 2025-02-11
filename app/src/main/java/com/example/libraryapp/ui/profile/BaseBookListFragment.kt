package com.example.libraryapp.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.libraryapp.databinding.FragmentReadFavoriteBooksBinding
import com.example.libraryapp.ui.list.adapter.BookAdapter

abstract class BaseBookListFragment : Fragment() {
    private var _binding: FragmentReadFavoriteBooksBinding? = null
    protected val binding get() = _binding!!
    protected val viewModel: ProfileViewModel by viewModels({ requireParentFragment() })
    protected lateinit var bookAdapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReadFavoriteBooksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(emptyList(), object : BookAdapter.RecyclerViewEvent {
            override fun onItemClick(bookId: String) {
                findNavController().navigate(
                    ProfileFragmentDirections.toDetailFragment(bookId.toString())
                )
            }
        })
        binding.recyclerView.adapter = bookAdapter
    }

    abstract fun observeViewModel()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
