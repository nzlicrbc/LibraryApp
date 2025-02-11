package com.example.libraryapp.ui.airecommend.model

import com.example.libraryapp.data.remote.model.GoogleBook

sealed class AiRecommendState {
    object Initial : AiRecommendState()
    object Loading : AiRecommendState()
    object Survey : AiRecommendState()
    data class Recommendations(val books: List<GoogleBook>) : AiRecommendState()
    data class Error(val message: String) : AiRecommendState()
}