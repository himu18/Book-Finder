package com.bookfinder.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bookfinder.app.ui.details.DetailsRoute
import com.bookfinder.app.ui.search.SearchRoute
import com.bookfinder.app.ui.favorites.FavoritesRoute

@Composable
fun BookFinderNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "search") {
        composable("search") {
            SearchRoute(navController)
        }
        composable(
            route = "details/{workId}",
            arguments = listOf(navArgument("workId") { type = NavType.StringType })
        ) {
            val workId = it.arguments?.getString("workId") ?: return@composable
            DetailsRoute(navController, workId)
        }
        composable("favorites") {
            FavoritesRoute(navController)
        }
    }
}


