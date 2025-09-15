package com.bookfinder.app

import com.bookfinder.app.domain.model.Book
import com.bookfinder.app.domain.repository.BookRepository
import com.bookfinder.app.ui.details.DetailsViewModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

private class FakeRepo : BookRepository {
    private val savedBooks = mutableSetOf<String>()
    
    override fun searchBooksPaged(query: String) = flowOf(PagingData.empty<Book>())
    override suspend fun getBookDetails(workId: String): Book = Book(id = "/works/$workId", title = "T", author = "A", coverUrl = null, publishYear = 2020)
    override fun observeSavedBooks() = flowOf(emptyList<Book>())
    override suspend fun saveBook(book: Book) { savedBooks.add(book.id) }
    override suspend fun removeBook(id: String) { savedBooks.remove(id) }
    override suspend fun isSaved(id: String) = savedBooks.contains(id)
}

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeRepo
    private lateinit var vm: DetailsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repo = FakeRepo()
        vm = DetailsViewModel(repo)
    }

    @Test
    fun load_and_toggleSave_updates_state() = runTest(dispatcher) {
        vm.load("OL1W")
        dispatcher.scheduler.advanceUntilIdle()
        
        val currentState = vm.state.value
        assertThat(currentState.book).isNotNull()
        assertThat(currentState.book?.title).isEqualTo("T")
        assertThat(currentState.isSaved).isFalse()
        
        vm.toggleSave()
        dispatcher.scheduler.advanceUntilIdle()
        assertThat(vm.state.value.isSaved).isTrue()
    }
}


