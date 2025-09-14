package com.bookfinder.app.ui.details

// Removed animation imports as rotation animation was removed
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
// Removed rotate import as rotation animation was removed
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@Composable
fun DetailsRoute(navController: NavController, workId: String, viewModel: DetailsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    androidx.compose.runtime.LaunchedEffect(workId) { viewModel.load(workId) }
    DetailsScreen(
        state = state,
        onBack = { navController.popBackStack() },
        onToggleSave = { viewModel.toggleSave() },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun DetailsScreen(
    state: DetailsState,
    onBack: () -> Unit,
    onToggleSave: () -> Unit,
) {
    // Debug logging
    android.util.Log.d("DetailsScreen", "Received state: isLoading=${state.isLoading}, book=${state.book}, error=${state.error}, isSaved=${state.isSaved}")
    
    // Removed rotation animation for better UX
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(state.book?.title ?: "Details") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = onToggleSave, enabled = state.book != null) {
                    if (state.isSaved) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = "Remove from favorites",
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.error
                        )
                    } else {
                        Icon(
                            Icons.Outlined.FavoriteBorder,
                            contentDescription = "Add to favorites"
                        )
                    }
                }
            }
        )
    }) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.book != null -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        GlideImage(
                            model = state.book.coverUrl,
                            contentDescription = state.book.title,
                            modifier = Modifier
                                .size(200.dp)
                                .padding(16.dp)
                        )
                        Text(
                            text = state.book.title,
                            modifier = Modifier.padding(16.dp)
                        )
                        state.book.author?.let {
                            Text(
                                text = "by $it",
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        state.book.publishYear?.let {
                            Text(
                                text = "Published: $it",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        state.book.description?.let {
                            Text(
                                text = it,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        if (state.book.author == null && state.book.publishYear == null && state.book.description == null) {
                            Text(
                                text = "Limited details available for this book",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                state.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error loading book details",
                                modifier = Modifier.padding(16.dp)
                            )
                            Text(
                                text = state.error,
                                modifier = Modifier.padding(8.dp)
                            )
                            Button(onClick = onBack, modifier = Modifier.padding(16.dp)) {
                                Text("Go Back")
                            }
                        }
                    }
                }
            }
        }
    }
}


