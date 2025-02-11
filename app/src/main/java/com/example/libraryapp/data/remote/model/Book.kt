package com.example.libraryapp.data.remote.model

import com.google.gson.annotations.SerializedName

data class Book(
    @SerializedName("key") val id: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("author_name") val authorNames: List<String>? = null,
    @SerializedName("first_publish_year") val publishYear: Int? = null,
    @SerializedName("cover_i") val coverId: String? = null,
    @SerializedName("subject") val subjects: List<String>? = null,
    @SerializedName("language") val languages: List<String>? = null,
    @SerializedName("ratings_average") val rating: Double? = null
) {
    fun getCoverUrl(): String? {
        return coverId?.let { "https://covers.openlibrary.org/b/id/$it-L.jpg" }
    }
}