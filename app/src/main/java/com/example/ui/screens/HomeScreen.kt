package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.network.TmdbShow
import com.example.ui.theme.*
import com.example.viewmodel.HomeViewModel

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = viewModel()) {
    val trendingShows by viewModel.trendingShows.collectAsState()
    val topRatedShows by viewModel.topRatedShows.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TRACKVERSE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueHighlight,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Hello, Alex",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            // Profile image
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark)
                    .border(1.dp, BorderStroke, CircleShape)
            ) {
                AsyncImage(
                    model = "https://api.dicebear.com/7.x/avataaars/svg?seed=Alex",
                    contentDescription = "Profile",
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .background(BlueHighlight, CircleShape)
                        .border(2.dp, BgDark, CircleShape)
                )
            }
        }

        // Quick Stats Summary
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard("128", "SHOWS", Modifier.weight(1f))
            StatCard("14.2d", "WATCHED", Modifier.weight(1f))
            StatCard("842", "EPISODES", Modifier.weight(1f))
        }

        // Continue Watching Section
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text("Trending Today", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text("View All", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = BlueHighlight)
        }

        // Hero Card
        val heroShow = trendingShows.firstOrNull()
        if (heroShow != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceDark)
                    .padding(bottom = 32.dp)
            ) {
                AsyncImage(
                    model = "https://image.tmdb.org/t/p/w780${heroShow.backdropPath ?: heroShow.posterPath}",
                    contentDescription = heroShow.name ?: heroShow.title,
                    modifier = Modifier.fillMaxSize().clickable {
                        val isMovie = heroShow.title != null
                        navController.navigate("details/${heroShow.id}/$isMovie")
                    },
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f), Color.Black)
                            )
                        )
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .align(Alignment.BottomStart)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text("★ ${heroShow.voteAverage ?: "N/A"}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BlueLight)
                            Text(heroShow.name ?: heroShow.title ?: "Unknown", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("TRENDING", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            }
        }

        // AI Recommendations
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Top Rated Shows", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (topRatedShows.isNotEmpty()) {
                val displays = topRatedShows.take(3)
                displays.forEach { show ->
                    AiRecItem(
                        title = show.name ?: show.title ?: "Unknown",
                        imageUrl = "https://image.tmdb.org/t/p/w342${show.posterPath}",
                        modifier = Modifier.weight(1f).clickable {
                            val isMovie = show.title != null
                            navController.navigate("details/${show.id}/$isMovie")
                        }
                    )
                }
            } else if (isLoading) {
                 CircularProgressIndicator(color = BlueHighlight, modifier = Modifier.align(Alignment.CenterVertically))
            } else if (error != null) {
                 Text("Error: $error", color = ErrorColor, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(SurfaceDark, RoundedCornerShape(16.dp))
            .border(1.dp, BorderStroke, RoundedCornerShape(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = BlueLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(label, color = TextTertiary, fontSize = 10.sp, letterSpacing = 0.5.sp)
        }
    }
}

@Composable
fun AiRecItem(title: String, imageUrl: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .border(1.dp, BorderStroke, RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                colorFilter = androidx.compose.ui.graphics.ColorFilter.colorMatrix(androidx.compose.ui.graphics.ColorMatrix().apply { setToSaturation(0.2f) })
            )
        }
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}
