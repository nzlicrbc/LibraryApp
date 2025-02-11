package com.example.libraryapp.data.remote

import retrofit2.HttpException
import android.util.Log
import com.example.libraryapp.BuildConfig
import com.example.libraryapp.ui.airecommend.model.UserPreferences
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GeminiService @Inject constructor() {
    private val model = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun getBookRecommendations(userPreferences: UserPreferences): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildPrompt(userPreferences)
                Log.d("GeminiService", "Sending prompt: $prompt")

                val response = model.generateContent(prompt).text?.toString() ?: ""
                Log.d("GeminiService", "Received response: $response")

                val recommendations = response.lines().filter { it.isNotBlank() }
                Log.d("GeminiService", "Parsed recommendations: $recommendations")

                recommendations
            } catch (e: Exception) {
                Log.e("GeminiService", "Error getting recommendations", e)
                emptyList()
            }
        }
    }

    private fun buildPrompt(preferences: UserPreferences): String {
        val englishGenres = preferences.favoriteGenres.map { genre ->
            when (genre) {
                "Roman" -> "Fiction"
                "Bilim Kurgu" -> "Science Fiction"
                "Polisiye" -> "Mystery"
                "Fantastik" -> "Fantasy"
                "Macera" -> "Adventure"
                "Romantik" -> "Romance"
                "Bilim" -> "Science"
                "Tarih" -> "History"
                "Felsefe" -> "Philosophy"
                "Psikoloji" -> "Psychology"
                "Biyografi" -> "Biography"
                "Klasikler" -> "Classics"
                else -> genre
            }
        }

        val englishMood = when (preferences.mood) {
            "Eğlenceli" -> "Fun and Light"
            "Düşündürücü" -> "Thought-provoking"
            "Rahatlatıcı" -> "Relaxing"
            else -> preferences.mood
        }

        return """
        You are a book recommendation expert. Please recommend internationally acclaimed and popular books that most readers would enjoy.

        USER PREFERENCES:
        - Genres: ${englishGenres.joinToString(", ")}
        - Style: $englishMood
        - Purpose: ${preferences.readingPurpose}

        RULES:
        1. Recommend 5 books that are:
           - Internationally acclaimed bestsellers
           - Easy to find and widely available
           - Suitable for general readers
           - Mix of classic and contemporary works
           - Engaging and well-rated
        2. Use EXACTLY this format:
           "Book Title - Author Name"
        3. One book per line
        4. Include a variety of genres from user's preferences
        5. Focus on books that are universally appreciated

        Provide ONLY the book list, no explanations.
    """.trimIndent()
    }
}
