package com.example.libraryapp.data.remote.model

data class GoogleBooksResponse(
    val items: List<GoogleBook>?,
    val totalItems: Int
)

data class GoogleBookDetailResponse(
    val id: String,
    val volumeInfo: VolumeInfo
)

data class GoogleBook(
    val id: String,
    val volumeInfo: VolumeInfo,
    val accessInfo: AccessInfo? = null
)

data class AccessInfo(
    val webReaderLink: String? = null
)

data class VolumeInfo(
    val title: String?,
    val authors: List<String>?,
    val publishedDate: String?,
    val description: String?,
    val pageCount: Int?,
    val categories: List<String>?,
    val imageLinks: ImageLinks?,
    val language: String?
)

data class ImageLinks(
    val smallThumbnail: String?,
    val thumbnail: String?
)