package com.example.libraryapp.data.mapper

import com.example.libraryapp.data.local.entity.SavedBookEntity
import com.example.libraryapp.data.remote.model.GoogleBook
import com.example.libraryapp.data.remote.model.ImageLinks
import com.example.libraryapp.data.remote.model.VolumeInfo

fun SavedBookEntity.toGoogleBook(): GoogleBook {
    return GoogleBook(
        id = this.bookId,
        volumeInfo = VolumeInfo(
            title = this.title,
            authors = listOf(this.author),
            publishedDate = null,
            description = null,
            pageCount = null,
            categories = null,
            imageLinks = ImageLinks(
                smallThumbnail = this.thumbnailUrl,
                thumbnail = this.thumbnailUrl
            ),
            language = null
        )
    )
}