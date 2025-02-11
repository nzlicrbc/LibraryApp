package com.example.libraryapp.ui.airecommend

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.data.repository.BookRepository
import com.example.libraryapp.ui.airecommend.model.AiRecommendState
import com.example.libraryapp.ui.airecommend.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiRecommendViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AiRecommendState>(AiRecommendState.Initial)
    val state: StateFlow<AiRecommendState> = _state

    var hasCompletedSurvey: Boolean = false
        private set

    private var userPreferences = UserPreferences()

    init {
        checkSurveyStatus()
    }

    private fun checkSurveyStatus() {
        viewModelScope.launch {
            _state.value = AiRecommendState.Survey
        }
    }

    fun updateReadingPurpose(purpose: String) {
        userPreferences = userPreferences.copy(readingPurpose = purpose)
    }

    fun updateGenres(genres: List<String>) {
        userPreferences = userPreferences.copy(favoriteGenres = genres)
    }

    fun updateMoodPreference(mood: String) {
        userPreferences = userPreferences.copy(mood = mood)
    }

    fun submitSurvey() {
        viewModelScope.launch {
            _state.value = AiRecommendState.Loading
            try {
                val recommendations = bookRepository.getAiRecommendations(userPreferences)
                if (recommendations.isEmpty()) {
                    _state.value = AiRecommendState.Error("No recommendations found. Please try selecting different genres.")
                    return@launch
                }
                _state.value = AiRecommendState.Recommendations(recommendations)
                hasCompletedSurvey = true
            } catch (e: Exception) {
                _state.value = AiRecommendState.Error("An error occurred while fetching book recommendations. Please try again.")
                Log.e("AiRecommendViewModel", "Error getting recommendations", e)
            }
        }
    }
}