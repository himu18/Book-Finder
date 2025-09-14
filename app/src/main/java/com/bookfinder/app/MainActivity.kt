package com.bookfinder.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.bookfinder.app.ui.navigation.BookFinderNavHost
import com.bookfinder.app.ui.theme.BookFinderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BookFinderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    BookFinderNavHost()
                }
            }
        }
    }
}