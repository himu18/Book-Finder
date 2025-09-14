package com.bookfinder.app.domain.repository

import androidx.paging.PagingData
import com.bookfinder.app.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun searchBooksPaged(query: String): Flow<PagingData<Book>>
    suspend fun getBookDetails(workId: String): Book
    fun observeSavedBooks(): Flow<List<Book>>
    suspend fun saveBook(book: Book)
    suspend fun removeBook(id: String)
    suspend fun isSaved(id: String): Boolean
}


