package com.bookfinder.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String?,
    val cover_url: String?,
    val publish_year: Int?,
    val description: String?,
    val is_saved: Boolean = true,
    val created_at: Long = System.currentTimeMillis(),
)


