package com.bookfinder.app.domain.usecase

import androidx.paging.PagingData
import com.bookfinder.app.domain.model.Book
import com.bookfinder.app.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow

class SearchBooksUseCase(private val repository: BookRepository) {
    operator fun invoke(query: String): Flow<PagingData<Book>> = repository.searchBooksPaged(query)
}

class GetBookDetailsUseCase(private val repository: BookRepository) {
    suspend operator fun invoke(workId: String): Book = repository.getBookDetails(workId)
}

class ToggleSaveBookUseCase(private val repository: BookRepository) {
    suspend operator fun invoke(book: Book) {
        if (repository.isSaved(book.id)) repository.removeBook(book.id) else repository.saveBook(book)
    }
}

class ObserveSavedBooksUseCase(private val repository: BookRepository) {
    operator fun invoke(): Flow<List<Book>> = repository.observeSavedBooks()
}


