package com.example.libraryapp

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenLibraryApi {

    @GET("search.json")
    fun searchBooks(
        @Query("q") query: String,
        @Query("page") page: Int
    ): Call<BookSearchResponse>
}
