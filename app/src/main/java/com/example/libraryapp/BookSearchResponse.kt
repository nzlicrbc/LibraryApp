package com.example.libraryapp

data class BookSearchResponse(
    val numFound: Int,  // Bulunan kitap sayısı
    val docs: List<Book> // Kitaplar listesi
)

data class Book(
    val title: String,             // Kitap başlığı
    val author_name: List<String>, // Yazar adı
    val cover_i: Int?             // Kitap kapağı ID'si
)
