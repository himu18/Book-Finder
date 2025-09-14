package com.bookfinder.app.data.remote

import com.bookfinder.app.data.remote.dto.BookDetailsDto
import com.bookfinder.app.data.remote.dto.SearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenLibraryApi {
    @GET("search.json")
    suspend fun searchBooks(
        @Query("title") query: String,
        @Query("limit") limit: Int,
        @Query("page") page: Int,
    ): SearchResponseDto

    @GET("works/{workId}.json")
    suspend fun getWorkDetails(
        @Path("workId") workId: String,
    ): BookDetailsDto
}


