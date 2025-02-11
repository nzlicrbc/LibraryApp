package com.example.libraryapp.ui.airecommend

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
        setupRadioButtons()
    }

    private fun setupRadioButtons() {
        binding.apply {
            moodLight.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.white))
            moodThought.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.white))
            moodRelaxing.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.white))

            moodLight.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            moodThought.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            moodRelaxing.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        }
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
    }

    private fun setupChips() {
        val purposes = listOf(
            "Personal Development", "Entertainment", "Gaining Knowledge",
            "Relaxation", "New Perspectives", "Motivation",
            "Culture & Arts", "Work & Career"
        )
        purposes.forEach { purpose ->
            val chip = Chip(requireContext()).apply {
                text = purpose
                isCheckable = true

                // Chip Design
                setTextAppearanceResource(com.google.android.material.R.style.TextAppearance_MaterialComponents_Body1)

                // Color settings
                setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.transparent))

                // Border settings
                chipStrokeWidth = resources.getDimension(R.dimen.chip_stroke_width)
                chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.white))

                // When selected, background is white, text is primary color
                checkedIcon = null
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.white))
                        setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    } else {
                        chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.transparent))
                        setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    }
                }

                alpha = 0f
                scaleX = 0.8f
                scaleY = 0.8f
                animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(300).start()
            }
            binding.purposeChipGroup.addView(chip)
        }

        val genres = listOf(
            "Fiction", "Science Fiction", "Mystery", "Fantasy",
            "Adventure", "Romance", "Science", "History",
            "Philosophy", "Psychology", "Biography", "Classics"
        )
        genres.forEach { genre ->
            val chip = Chip(requireContext()).apply {
                text = genre
                isCheckable = true

                // Chip Design
                setTextAppearanceResource(com.google.android.material.R.style.TextAppearance_MaterialComponents_Body1)

                // Color settings
                setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.transparent))

                // Border settings
                chipStrokeWidth = resources.getDimension(R.dimen.chip_stroke_width)
                chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.white))

                // When selected, background is white, text is primary color
                checkedIcon = null
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.white))
                        setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    } else {
                        chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.transparent))
                        setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    }
                }

                alpha = 0f
                scaleX = 0.8f
                scaleY = 0.8f
                animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(300).start()
            }
            binding.genreChipGroup.addView(chip)
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
        }
    }

    private fun showSurvey() {
        fadeInView(binding.surveyContainer)
        binding.apply {
            progressBar.isVisible = false
            recommendationsRecycler.isVisible = false
        }
    }

    private fun showRecommendations(books: List<GoogleBook>) {
        fadeInView(binding.recommendationsRecycler)
        binding.apply {
            progressBar.isVisible = false
            surveyContainer.isVisible = false
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
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun fadeInView(view: View, duration: Long = 500) {
        view.alpha = 0f
        view.isVisible = true
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .start()
    }

    private fun fadeOutView(view: View, duration: Long = 500) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .withEndAction { view.isVisible = false }
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