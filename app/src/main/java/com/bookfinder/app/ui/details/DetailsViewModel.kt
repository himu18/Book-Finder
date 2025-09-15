package com.bookfinder.app.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookfinder.app.domain.model.Book
import com.bookfinder.app.domain.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailsState(
    val isLoading: Boolean = false,
    val book: Book? = null,
    val error: String? = null,
    val isSaved: Boolean = false,
)

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: BookRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(DetailsState())
    val state: StateFlow<DetailsState> = _state

    fun load(workId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            runCatching {
                val book = repository.getBookDetails(workId)
                val saved = repository.isSaved(book.id)
                book to saved
            }.onSuccess { (book, saved) ->
                _state.value = DetailsState(isLoading = false, book = book, isSaved = saved)
            }.onFailure { t ->
                _state.value = DetailsState(
                    isLoading = false, 
                    error = "Failed to load book details: ${t.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun toggleSave() {
        val book = _state.value.book ?: return
        viewModelScope.launch {
            try {
                if (_state.value.isSaved) {
                    repository.removeBook(book.id)
                    _state.value = _state.value.copy(isSaved = false)
                } else {
                    repository.saveBook(book)
                    _state.value = _state.value.copy(isSaved = true)
                }
            } catch (e: Exception) {
                // Handle error silently or update state with error message
                _state.value = _state.value.copy(error = "Failed to toggle save state: ${e.message}")
            }
        }
    }
}


