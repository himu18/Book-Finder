package com.bookfinder.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SearchResponseDto(
    @SerializedName("docs") val docs: List<SearchDocDto>,
    @SerializedName("numFound") val numFound: Int,
)

data class SearchDocDto(
    @SerializedName("title") val title: String?,
    @SerializedName("author_name") val authorName: List<String>?,
    @SerializedName("cover_i") val coverId: Long?,
    @SerializedName("key") val key: String?, // e.g. "/works/OL468516W"
    @SerializedName("first_publish_year") val firstPublishYear: Int?,
    @SerializedName("isbn") val isbns: List<String>?,
)

data class BookDetailsDto(
    @SerializedName("description") val description: Any?,
    @SerializedName("title") val title: String?,
    @SerializedName("authors") val authors: List<Map<String, Any>>?,
    @SerializedName("covers") val covers: List<Int>?,
    @SerializedName("first_publish_date") val firstPublishDate: String?,
    @SerializedName("subjects") val subjects: List<String>?,
    @SerializedName("created") val created: Map<String, Any>?,
)


