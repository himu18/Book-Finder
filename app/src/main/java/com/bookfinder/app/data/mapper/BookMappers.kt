package com.bookfinder.app.data.mapper

import com.bookfinder.app.domain.model.Book
import com.bookfinder.app.data.remote.dto.SearchDocDto
import com.bookfinder.app.data.remote.dto.BookDetailsDto

private fun coverUrlFromId(coverId: Long?): String? = coverId?.let { "https://covers.openlibrary.org/b/id/${it}-M.jpg" }

private fun parseDescription(desc: Any?): String? = when (desc) {
    is String -> desc
    is Map<*, *> -> desc["value"] as? String
    else -> null
}

fun SearchDocDto.toDomain(): Book? {
    val workKey = key ?: return null
    return Book(
        id = workKey,
        title = title ?: "",
        author = authorName?.firstOrNull(),
        coverUrl = coverUrlFromId(coverId),
        publishYear = firstPublishYear,
        description = null,
    )
}

fun Book.withDetails(dto: BookDetailsDto): Book = copy(
    title = dto.title ?: title,
    description = parseDescription(dto.description) ?: description,
)


