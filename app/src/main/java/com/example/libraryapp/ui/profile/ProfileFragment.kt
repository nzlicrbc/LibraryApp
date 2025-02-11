package com.example.libraryapp.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.libraryapp.R
import com.example.libraryapp.data.auth.AuthManager
import com.example.libraryapp.databinding.FragmentProfileBinding
import com.example.libraryapp.ui.detail.DetailViewModel
import com.example.libraryapp.ui.profile.adapter.ProfilePagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var pagerAdapter: ProfilePagerAdapter
    @Inject
    lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        setupTabLayout()
        observeViewModel()
        observeDetailUpdates()
        setupLogout()
        animateInitialViews()
    }

    private fun setupLogout() {
        binding.fabLogout.setOnClickListener {
            animateClickEffect(binding.fabLogout)
            authManager.logout()
            findNavController().navigate(R.id.to_loginFragment)
        }
    }

    private fun observeDetailUpdates() {
        viewLifecycleOwner.lifecycleScope.launch {
            val detailViewModel: DetailViewModel by viewModels(ownerProducer = { requireActivity() })
            detailViewModel.updateProfile.collect {
                viewModel.loadBooks()
            }
        }
    }

    private fun setupViewPager() {
        pagerAdapter = ProfilePagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        fadeInView(binding.viewPager)
    }

    private fun setupTabLayout() {
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.saved_books_label)
                1 -> getString(R.string.favorite_count_label)
                2 -> getString(R.string.read_books_label)
                else -> throw IllegalStateException("Invalid position")
            }
        }.attach()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.stats.collect { stats ->
                fadeInView(binding.textReadingCount)
                fadeInView(binding.textFavoriteCount)
                fadeInView(binding.textReadCount)
                binding.textReadingCount.text = stats.savedCount.toString()
                binding.textFavoriteCount.text = stats.favoriteCount.toString()
                binding.textReadCount.text = stats.readCount.toString()
            }
        }
    }

    private fun animateInitialViews() {
        fadeInView(binding.tabLayout)
    }

    private fun fadeInView(view: View, duration: Long = 500) {
        view.alpha = 0f
        view.isVisible = true
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .start()
    }

    private fun animateClickEffect(view: View, duration: Long = 150) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(duration)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(duration)
                    .start()
            }
            .start()
    }
}