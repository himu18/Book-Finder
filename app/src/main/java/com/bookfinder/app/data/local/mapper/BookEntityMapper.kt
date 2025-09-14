package com.bookfinder.app.data.local.mapper

import com.bookfinder.app.data.local.entity.BookEntity
import com.bookfinder.app.domain.model.Book

fun Book.toEntity(): BookEntity = BookEntity(
    id = id,
    title = title,
    author = author,
    cover_url = coverUrl,
    publish_year = publishYear,
    description = description,
    is_saved = true,
)

fun BookEntity.toDomain(): Book = Book(
    id = id,
    title = title,
    author = author,
    coverUrl = cover_url,
    publishYear = publish_year,
    description = description,
    isSaved = is_saved,
)


