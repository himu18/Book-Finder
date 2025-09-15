package com.bookfinder.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bookfinder.app.domain.model.Book
import com.bookfinder.app.domain.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: BookRepository,
) : ViewModel() {

    private val queryState = MutableStateFlow("")
    private val refreshTrigger = MutableStateFlow(0)

    @OptIn(FlowPreview::class)
    val results: StateFlow<PagingData<Book>> = queryState
        .debounce(300)
        .flatMapLatest { q ->
            if (q.isBlank()) {
                kotlinx.coroutines.flow.flowOf(PagingData.empty())
            } else {
                repository.searchBooksPaged(q)
            }
        }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope, 
            started = SharingStarted.WhileSubscribed(5000), 
            initialValue = PagingData.empty()
        )

    val query: StateFlow<String> = queryState

    fun onQueryChange(newQuery: String) {
        queryState.value = newQuery
    }

    fun refresh() {
        refreshTrigger.value++
        val currentQuery = queryState.value
        queryState.value = ""
        queryState.value = currentQuery
    }
}


