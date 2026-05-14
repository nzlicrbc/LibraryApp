package com.example.libraryapp.ui.airecommend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import com.example.libraryapp.R
import com.example.libraryapp.data.remote.model.GoogleBook
import com.example.libraryapp.databinding.FragmentAiRecommendBinding
import com.example.libraryapp.ui.airecommend.model.AiRecommendState
import com.example.libraryapp.ui.list.adapter.BookAdapter
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AiRecommendFragment : Fragment() {
    private lateinit var binding: FragmentAiRecommendBinding
    private val viewModel: AiRecommendViewModel by viewModels()
    private lateinit var bookAdapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAiRecommendBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupChips()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(emptyList(), object : BookAdapter.RecyclerViewEvent {
            override fun onItemClick(bookId: String) {
                findNavController().navigate(
                    AiRecommendFragmentDirections.toDetailFragment(bookId)
                )
            }
        })
        binding.recommendationsRecycler.adapter = bookAdapter
        binding.recommendationsRecycler.itemAnimator = DefaultItemAnimator().apply {
            addDuration = 240
            removeDuration = 200
            changeDuration = 200
            moveDuration = 280
        }
    }

    private fun setupChips() {
        val purposes = listOf(
            "Personal Development",
            "Entertainment",
            "Gaining Knowledge",
            "Relaxation",
            "New Perspectives",
            "Motivation",
            "Culture & Arts",
            "Work & Career"
        )
        purposes.forEachIndexed { index, purpose ->
            binding.purposeChipGroup.addView(createFilterChip(purpose, index))
        }

        val genres = listOf(
            "Fiction",
            "Science Fiction",
            "Mystery",
            "Fantasy",
            "Adventure",
            "Romance",
            "Science",
            "History",
            "Philosophy",
            "Psychology",
            "Biography",
            "Classics"
        )
        genres.forEachIndexed { index, genre ->
            binding.genreChipGroup.addView(
                createFilterChip(genre, index + purposes.size)
            )
        }
    }

    private fun createFilterChip(label: String, staggerIndex: Int): Chip {
        val ctx = requireContext()
        return Chip(ctx, null, com.google.android.material.R.attr.chipStyle).apply {
            text = label
            isCheckable = true
            checkedIcon = null
            chipStartPadding = resources.getDimension(R.dimen.padding_8)
            chipEndPadding = resources.getDimension(R.dimen.padding_8)
            chipStrokeWidth = resources.getDimension(R.dimen.chip_stroke_width)
            chipBackgroundColor = ContextCompat.getColorStateList(ctx, R.color.ai_filter_chip_background)
            chipStrokeColor = ContextCompat.getColorStateList(ctx, R.color.ai_filter_chip_stroke)
            setTextColor(ContextCompat.getColorStateList(ctx, R.color.ai_filter_chip_text))
            setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
            alpha = 0f
            scaleX = 0.92f
            scaleY = 0.92f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(280)
                .setStartDelay((staggerIndex * 35L).coerceAtMost(400))
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        }
    }

    private fun setupListeners() {
        binding.submitButton.setOnClickListener {
            animateClickEffect(it)

            val selectedGenres = binding.genreChipGroup.checkedChipIds.mapNotNull { id ->
                (binding.genreChipGroup.findViewById<Chip>(id))?.text?.toString()
            }

            if (selectedGenres.isEmpty()) {
                Toast.makeText(
                    context,
                    "Please select at least one genre",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            val mood = when (binding.moodGroup.checkedRadioButtonId) {
                R.id.mood_light -> "Fun and Light"
                R.id.mood_thought -> "Thought-provoking"
                else -> "Relaxing"
            }

            viewModel.apply {
                updateGenres(selectedGenres)
                updateMoodPreference(mood)
                submitSurvey()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is AiRecommendState.Initial -> Unit
                    is AiRecommendState.Loading -> showLoading()
                    is AiRecommendState.Survey -> showSurvey()
                    is AiRecommendState.Recommendations -> showRecommendations(state.books)
                    is AiRecommendState.Error -> showError(state.message)
                }
            }
        }
    }

    private fun showLoading() {
        fadeInView(binding.progressBar)
        binding.apply {
            surveyContainer.isVisible = false
            recommendationsRecycler.isVisible = false
            aiToolbar.isVisible = true
        }
    }

    private fun showSurvey() {
        fadeInView(binding.surveyContainer)
        binding.apply {
            progressBar.isVisible = false
            recommendationsRecycler.isVisible = false
            aiToolbar.isVisible = true
        }
    }

    private fun showRecommendations(books: List<GoogleBook>) {
        fadeInView(binding.recommendationsRecycler)
        binding.apply {
            progressBar.isVisible = false
            surveyContainer.isVisible = false
            aiToolbar.isVisible = true
            bookAdapter.updateData(books)
        }

        if (books.isEmpty()) {
            Toast.makeText(
                context,
                "Sorry, no books matched your criteria. Please try different selections.",
                Toast.LENGTH_LONG
            ).show()
            showSurvey()
        }
    }

    private fun showError(message: String) {
        fadeOutView(binding.progressBar)
        fadeInView(binding.surveyContainer)
        binding.recommendationsRecycler.isVisible = false
        binding.aiToolbar.isVisible = true
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun fadeInView(view: View, duration: Long = 420) {
        view.alpha = 0f
        view.isVisible = true
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()
    }

    private fun fadeOutView(view: View, duration: Long = 320) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .withEndAction { view.isVisible = false }
            .start()
    }

    private fun animateClickEffect(view: View, duration: Long = 150) {
        view.animate()
            .scaleX(0.97f)
            .scaleY(0.97f)
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
