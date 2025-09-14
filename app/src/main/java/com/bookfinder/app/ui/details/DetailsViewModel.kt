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
        android.util.Log.d("DetailsViewModel", "Loading book details for workId: $workId")
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            runCatching {
                val book = repository.getBookDetails(workId)
                android.util.Log.d("DetailsViewModel", "Repository returned book: $book")
                val saved = repository.isSaved(book.id)
                android.util.Log.d("DetailsViewModel", "Book saved status: $saved")
                book to saved
            }.onSuccess { (book, saved) ->
                android.util.Log.d("DetailsViewModel", "Successfully loaded book, updating state")
                _state.value = DetailsState(isLoading = false, book = book, isSaved = saved)
                android.util.Log.d("DetailsViewModel", "State updated: ${_state.value}")
            }.onFailure { t ->
                android.util.Log.e("DetailsViewModel", "Failed to load book details", t)
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
                android.util.Log.e("DetailsViewModel", "Error toggling save state", e)
            }
        }
    }
}


