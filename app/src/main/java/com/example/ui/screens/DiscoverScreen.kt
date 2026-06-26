package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ui.theme.*
import com.example.viewmodel.DiscoverViewModel

@Composable
fun DiscoverScreen(navController: NavController, viewModel: DiscoverViewModel = viewModel()) {
    val trendingShows by viewModel.trendingShows.collectAsState()
    val popularShows by viewModel.popularShows.collectAsState()
    val topRatedShows by viewModel.topRatedShows.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        androidx.compose.foundation.lazy.LazyColumn {
            item {
                Text("Discover", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BlueHighlight)
                    }
                }
            } else {
                item {
                    DiscoverSection("Trending Shows", "trending", shows = trendingShows, navController)
                }
                item {
                    DiscoverSection("Popular Shows", "popular", shows = popularShows, navController)
                }
                item {
                    DiscoverSection("Top Rated Shows", "toprated", shows = topRatedShows, navController)
                }
            }
        }
    }
}

@Composable
fun DiscoverSection(title: String, sectionKey: String, shows: List<com.example.network.TmdbShow>, navController: NavController) {
    if (shows.isEmpty()) return
    Text(title, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    Spacer(modifier = Modifier.height(16.dp))
    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items(shows.size) { index ->
            val show = shows[index]
            SearchResultItem(
                show = show,
                sourceKey = sectionKey,
                modifier = Modifier
                    .width(120.dp)
                    .clickable {
                        val isMovie = show.title != null
                        navController.navigate("details/${show.id}/$isMovie?source=${sectionKey}")
                    }
            )
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
}
