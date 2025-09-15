package com.bookfinder.app.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.bookfinder.app.data.local.dao.BookDao
import com.bookfinder.app.data.local.mapper.toDomain
import com.bookfinder.app.data.local.mapper.toEntity
import com.bookfinder.app.data.mapper.toDomain
import com.bookfinder.app.data.remote.BookApiInterface
import com.bookfinder.app.data.remote.dto.SearchDocDto
import com.bookfinder.app.domain.model.Book
import com.bookfinder.app.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private class SearchPagingSource(
    private val api: BookApiInterface,
    private val query: String,
) : PagingSource<Int, Book>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {
        return try {
            val page = params.key ?: 1
            val pageSize = 20
            val response = api.searchBooks(query = query, limit = pageSize, page = page)
            val items = response.docs.mapNotNull(SearchDocDto::toDomain)
            
            val totalItems = response.numFound
            val hasMorePages = items.size == pageSize && items.isNotEmpty()

            android.util.Log.d("SearchPagingSource", "API Call: https://openlibrary.org/search.json?title=$query&limit=$pageSize&page=$page")
            android.util.Log.d("SearchPagingSource", "Page: $page, PageSize: $pageSize, Items: ${items.size}, Total: $totalItems, HasMore: $hasMorePages")
            android.util.Log.d("SearchPagingSource", "NextKey: ${if (hasMorePages) page + 1 else null}")
            
            LoadResult.Page(
                data = items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (hasMorePages) page + 1 else null,
            )
        } catch (t: Throwable) {
            android.util.Log.e("SearchPagingSource", "Error loading page ${params.key}", t)
            LoadResult.Error(t)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Book>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}

class BookRepositoryImpl @Inject constructor(
    private val api: BookApiInterface,
    private val bookDao: BookDao,
) : BookRepository {
    override fun searchBooksPaged(query: String): Flow<PagingData<Book>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 3,
                initialLoadSize = 20,
                maxSize = 200
            )
        ) { 
            SearchPagingSource(api, query) 
        }.flow

    override suspend fun getBookDetails(workId: String): Book {
        val id = workId.removePrefix("/works/")
        android.util.Log.d("BookRepository", "Loading book details for workId: $workId, id: $id")
        
        try {
            val details = api.getWorkDetails(id)
            android.util.Log.d("BookRepository", "API response received: title=${details.title}")
            android.util.Log.d("BookRepository", "Authors data: ${details.authors}")
            android.util.Log.d("BookRepository", "Covers data: ${details.covers}")
            android.util.Log.d("BookRepository", "Created data: ${details.created}")
            android.util.Log.d("BookRepository", "First publish date: ${details.firstPublishDate}")

            val authorName = details.authors?.firstOrNull()?.let { authorMap ->
                android.util.Log.d("BookRepository", "Processing author map: $authorMap")
                when {
                    authorMap.containsKey("name") -> {
                        android.util.Log.d("BookRepository", "Found direct name field")
                        authorMap["name"] as? String
                    }
                    authorMap.containsKey("author") -> {
                        android.util.Log.d("BookRepository", "Found author object")
                        val authorObj = authorMap["author"] as? Map<String, Any>
                        android.util.Log.d("BookRepository", "Author object: $authorObj")
                        when {
                            authorObj?.containsKey("name") == true -> authorObj["name"] as? String
                            authorObj?.containsKey("key") == true -> {
                                val authorKey = authorObj["key"] as? String
                                android.util.Log.d("BookRepository", "Author key: $authorKey")
                                authorKey?.let { key ->
                                    if (key.startsWith("/authors/")) {
                                        key.removePrefix("/authors/")
                                    } else {
                                        key
                                    }
                                }
                            }
                            else -> null
                        }
                    }
                    else -> {
                        android.util.Log.d("BookRepository", "No known author structure found")
                        null
                    }
                }
            }
            android.util.Log.d("BookRepository", "Final extracted author: $authorName")

            val coverUrl = details.covers?.firstOrNull()?.let { coverId ->
                "https://covers.openlibrary.org/b/id/${coverId}-M.jpg"
            }
            android.util.Log.d("BookRepository", "Extracted cover URL: $coverUrl")
            
            val publishYear = details.created?.get("value")?.let { createdValue ->
                try {
                    val dateString = createdValue as? String
                    dateString?.substring(0, 4)?.toInt()
                } catch (e: Exception) {
                    null
                }
            } ?: details.firstPublishDate?.let { date ->
                try {
                    date.substring(0, 4).toInt()
                } catch (e: Exception) {
                    null
                }
            }
            android.util.Log.d("BookRepository", "Extracted publish year: $publishYear")

            val description = when (details.description) {
                is String -> details.description
                is Map<*, *> -> details.description["value"] as? String
                else -> null
            }
            android.util.Log.d("BookRepository", "Extracted description: ${description?.take(100)}...")

            val book = Book(
                id = "/works/$id",
                title = details.title ?: "Unknown Title",
                author = authorName,
                coverUrl = coverUrl,
                publishYear = publishYear,
                description = description
            )
            android.util.Log.d("BookRepository", "Created book: $book")
            return book
        } catch (e: Exception) {
            android.util.Log.e("BookRepository", "Error loading book details for $workId", e)
            val fallbackBook = Book(
                id = "/works/$id",
                title = "Unknown Title",
                author = null,
                coverUrl = null,
                publishYear = null,
                description = null
            )
            android.util.Log.d("BookRepository", "Returning fallback book: $fallbackBook")
            return fallbackBook
        }
    }

    override fun observeSavedBooks(): Flow<List<Book>> =
        bookDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun saveBook(book: Book) {
        bookDao.upsert(book.toEntity())
    }

    override suspend fun removeBook(id: String) {
        bookDao.deleteById(id)
    }

    override suspend fun isSaved(id: String): Boolean = bookDao.isSaved(id)
}


