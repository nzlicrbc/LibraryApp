package com.example.libraryapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://openlibrary.org/"

    // API'yi oluşturuyoruz
    val api: OpenLibraryApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Gson ile dönüşüm
            .build()

        retrofit.create(OpenLibraryApi::class.java)
    }
}
