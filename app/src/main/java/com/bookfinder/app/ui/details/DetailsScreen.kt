package com.bookfinder.app.ui.details

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
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
    
    // Vertical flip animation for book cover
    val flipRotation by rememberInfiniteTransition("flip").animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 3000, easing = LinearEasing))
    )
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Book Cover with vertical flip animation
                        GlideImage(
                            model = state.book.coverUrl,
                            contentDescription = state.book.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .size(300.dp)
                                .graphicsLayer {
                                    rotationY = flipRotation
                                    cameraDistance = 12f * density
                                }
                        )
                        
                        // Title
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Title:",
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(0.3f)
                            )
                            Text(
                                text = state.book.title,
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.weight(0.7f)
                            )
                        }
                        
                        // Author
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Author:",
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(0.3f)
                            )
                            Text(
                                text = state.book.author ?: "Unknown Author",
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.weight(0.7f)
                            )
                        }
                        
                        // Publication Year
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Year:",
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(0.3f)
                            )
                            Text(
                                text = state.book.publishYear?.toString() ?: "Unknown Year",
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.weight(0.7f)
                            )
                        }
                        
                        // Description
                        state.book.description?.let { description ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Description:",
                                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = description,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        
                        // Fallback message if no data available
                        if (state.book.author == null && state.book.publishYear == null && state.book.description == null) {
                            Text(
                                text = "Limited details available for this book",
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
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


