package com.bookfinder.app.domain.model

data class Book(
    val id: String, // OpenLibrary work key, e.g. "/works/OL468516W"
    val title: String,
    val author: String?,
    val coverUrl: String?,
    val publishYear: Int?,
    val description: String? = null,
    val isSaved: Boolean = false,
)


