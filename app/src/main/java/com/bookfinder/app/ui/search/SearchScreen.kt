package com.bookfinder.app.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.bookfinder.app.R
import com.bookfinder.app.domain.model.Book
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun SearchRoute(navController: NavController, viewModel: SearchViewModel = hiltViewModel()) {
    val query by viewModel.query.collectAsState()
    val books = viewModel.results.collectAsLazyPagingItems()

    SearchScreen(
        query = query,
        onQueryChange = viewModel::onQueryChange,
        books = books,
        onClick = { book -> navController.navigate("details/${book.id.removePrefix("/works/")}") },
        onRefresh = { viewModel.refresh() },
        onFavoritesClick = { navController.navigate("favorites") }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun SearchScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    books: LazyPagingItems<Book>,
    onClick: (Book) -> Unit,
    onRefresh: () -> Unit,
    onFavoritesClick: () -> Unit,
) {
    val gridState = books.rememberLazyGridStateWithWorkaround()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Finder") },
                actions = {
                    IconButton(onClick = onFavoritesClick) {
                        Icon(Icons.Default.Favorite, contentDescription = "Favorites")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                singleLine = true,
                placeholder = { Text("Search books by title") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
            )

            when {
                query.isBlank() -> {
                    EmptySearchState()
                }

                books.loadState.refresh is androidx.paging.LoadState.Loading -> {
                    LoadingState()
                }

                books.loadState.refresh is androidx.paging.LoadState.Error -> {
                    ErrorState(
                        error = (books.loadState.refresh as androidx.paging.LoadState.Error).error,
                        onRetry = onRefresh
                    )
                }

                books.itemCount == 0 -> {
                    EmptyResultsState(query = query)
                }

                else -> {
                    SwipeRefresh(
                        state = rememberSwipeRefreshState(
                            isRefreshing = books.loadState.refresh is androidx.paging.LoadState.Loading
                        ),
                        onRefresh = onRefresh,
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 160.dp),
                            state = gridState,
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                count = books.itemCount,
                                key = books.itemKey { book -> book.id }  // stable key from item
                            ) { index ->
                                val book = books[index]
                                if (book != null) {
                                    BookCard(book = book, onClick = { onClick(book) })
                                }
                            }

                            when (val appendState = books.loadState.append) {
                                is androidx.paging.LoadState.Loading -> {
                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = androidx.compose.ui.Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }

                                is androidx.paging.LoadState.Error -> {
                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = androidx.compose.ui.Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "Failed to load more books",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                                Button(onClick = { books.retry() }) {
                                                    Text("Retry")
                                                }
                                            }
                                        }
                                    }
                                }

                                else -> Unit
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun <T : Any> LazyPagingItems<T>.rememberLazyGridStateWithWorkaround(): androidx.compose.foundation.lazy.grid.LazyGridState {
    return when (this.itemCount) {
        0 -> remember(this) { androidx.compose.foundation.lazy.grid.LazyGridState(0, 0) }
        else -> androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    }
}

@Composable
private fun EmptySearchState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
            Text("Search for books", modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    error: Throwable,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Text("Error loading books", modifier = Modifier.padding(16.dp))
            Text(error.message ?: "Unknown error", modifier = Modifier.padding(8.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyResultsState(query: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
            Text("No books found for \"$query\"", modifier = Modifier.padding(16.dp))
            Text("Try a different search term", modifier = Modifier.padding(8.dp))
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun BookCard(
    book: Book,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
            .height(220.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (book.coverUrl != null) {
                GlideImage(
                    model = book.coverUrl,
                    contentDescription = book.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_book_placeholder),
                    contentDescription = "Book placeholder",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Title:",
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    ),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.wrapContentWidth()
                )
                Text(
                    text = book.title,
                    style = androidx.compose.material3.MaterialTheme.typography.titleSmall.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    ),
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            book.author?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Author:",
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        ),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.wrapContentWidth()
                    )
                    Text(
                        text = it,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        ),
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}


