package com.bookfinder.app.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.valentinilk.shimmer.shimmer
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.LazyPagingItems
import com.bookfinder.app.domain.model.Book

@Composable
fun SearchRoute(navController: NavController, viewModel: SearchViewModel = hiltViewModel()) {
    val query by viewModel.query.collectAsState()
    val items = viewModel.results.collectAsLazyPagingItems()
    SearchScreen(
        query = query,
        onQueryChange = viewModel::onQueryChange,
        items = items,
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
    items: LazyPagingItems<Book>,
    onClick: (Book) -> Unit,
    onRefresh: () -> Unit,
    onFavoritesClick: () -> Unit,
) {
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
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                singleLine = true,
                placeholder = { Text("Search by title") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
            )
            
            when {
                query.isBlank() -> {
                    EmptySearchState()
                }
                items.loadState.refresh is androidx.paging.LoadState.Loading -> {
                    LoadingState()
                }
                items.loadState.refresh is androidx.paging.LoadState.Error -> {
                    ErrorState(
                        error = (items.loadState.refresh as androidx.paging.LoadState.Error).error,
                        onRetry = onRefresh
                    )
                }
                items.itemCount == 0 -> {
                    EmptyResultsState(query = query)
                }
                else -> {
                    SwipeRefresh(
                        state = rememberSwipeRefreshState(
                            isRefreshing = items.loadState.refresh is androidx.paging.LoadState.Loading
                        ),
                        onRefresh = onRefresh,
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 140.dp),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(items.itemSnapshotList.items) { book ->
                                BookCard(
                                    book = book,
                                    onClick = { onClick(book) }
                                )
                            }
                            
                            // Pagination loader
                            when (items.loadState.append) {
                                is androidx.paging.LoadState.Loading -> {
                                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                        androidx.compose.foundation.layout.Box(
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
                                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                        androidx.compose.foundation.layout.Box(
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
                                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                                Button(onClick = { items.retry() }) {
                                                    Text("Retry")
                                                }
                                            }
                                        }
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySearchState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
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
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
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
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
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
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlideImage(
                model = book.coverUrl,
                contentDescription = book.title,
                modifier = Modifier.fillMaxWidth()
            )
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Title:",
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    ),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(0.3f)
                )
                Text(
                    text = book.title,
                    style = androidx.compose.material3.MaterialTheme.typography.titleSmall.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    ),
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(0.7f)
                )
            }
            book.author?.let { 
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Author:",
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        ),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(0.3f)
                    )
                    Text(
                        text = it,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(0.7f)
                    )
                }
            }
        }
    }
}


