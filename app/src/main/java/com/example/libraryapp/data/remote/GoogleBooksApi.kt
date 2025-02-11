package com.example.libraryapp.data.remote

import com.example.libraryapp.BuildConfig
import com.example.libraryapp.data.remote.model.GoogleBook
import com.example.libraryapp.data.remote.model.GoogleBooksResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GoogleBooksApi {
    @GET("books/v1/volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 40,
        @Query("startIndex") startIndex: Int = 0,
        @Query("orderBy") orderBy: String = "relevance",
        @Query("key") apiKey: String = BuildConfig.GOOGLE_API_KEY
    ): GoogleBooksResponse

    @GET("books/v1/volumes/{volumeId}")
    suspend fun getBookDetails(
        @Path("volumeId") volumeId: String,
        @Query("key") apiKey: String = BuildConfig.GOOGLE_API_KEY
    ): GoogleBook
}