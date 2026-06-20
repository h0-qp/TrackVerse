package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ui.theme.*
import com.example.viewmodel.DetailsViewModel
import com.example.viewmodel.WatchlistViewModel

@Composable
fun DetailsScreen(
    showId: Int,
    isMovie: Boolean,
    onBack: () -> Unit,
    detailsViewModel: DetailsViewModel = viewModel(),
    watchlistViewModel: WatchlistViewModel = viewModel()
) {
    val show by detailsViewModel.show.collectAsState()
    val isLoading by detailsViewModel.isLoading.collectAsState()
    val error by detailsViewModel.error.collectAsState()
    val watchlist by watchlistViewModel.watchlist.collectAsState()

    LaunchedEffect(showId) {
        detailsViewModel.loadDetails(showId, isMovie)
        watchlistViewModel.loadWatchlist()
    }

    val isInWatchlist = watchlist.any { it.id == show?.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(SurfaceDark)
        ) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w780${show?.posterPath}",
                contentDescription = show?.name ?: show?.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha=0.6f), Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )

            // Back button
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .padding(top = 48.dp, start = 24.dp)
                    .size(32.dp)
                    .clickable { onBack() }
            )

            if (isLoading) {
                CircularProgressIndicator(
                    color = BlueHighlight,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        if (show != null) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = show?.name ?: show?.title ?: "Unknown",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "★ ${show?.voteAverage?.toString()?.take(3) ?: "N/A"}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BlueLight
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .background(SurfaceDark, RoundedCornerShape(8.dp))
                            .border(1.dp, BorderStroke, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(if (isMovie) "MOVIE" else "TV SHOW", fontSize = 10.sp, color = TextTertiary)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Overview", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = show?.overview ?: "No overview available.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                val trackedItem = watchlist.find { it.id == show?.id }
                val isInWatchlist = trackedItem != null
                val watchedCount = watchlistViewModel.watchedEpisodesCount.collectAsState().value[show?.id] ?: 0

                Button(
                    onClick = {
                        if (isInWatchlist) {
                            watchlistViewModel.removeFromWatchlist(show!!.id)
                        } else {
                            watchlistViewModel.addToWatchlist(show!!, isTracking = true)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isInWatchlist) SurfaceDark else BlueHighlight,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(
                        if (isInWatchlist) "Unsubscribe / Remove" else "Subscribe to Series",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                if (isInWatchlist && !isMovie) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Tracking", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceDark, RoundedCornerShape(16.dp))
                            .border(1.dp, BorderStroke, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Episodes Watched", fontSize = 14.sp, color = TextSecondary)
                                Text("$watchedCount / ${show?.numberOfEpisodes ?: "?"}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BlueHighlight)
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { if (watchedCount > 0) watchlistViewModel.updateWatchedEpisodes(show!!.id, watchedCount - 1) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BgDark, contentColor = TextPrimary),
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Text("-1", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Button(
                                    onClick = { watchlistViewModel.updateWatchedEpisodes(show!!.id, watchedCount + 1) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BlueHighlight, contentColor = TextPrimary),
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Text("+1", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                if (!isMovie && show?.nextEpisodeToAir != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Next Episode", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(BlueHighlight.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(show?.nextEpisodeToAir?.episodeNumber?.toString() ?: "", color = BlueHighlight, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(show?.nextEpisodeToAir?.name ?: "TBA", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            Text("Airs: ${show?.nextEpisodeToAir?.airDate ?: "Unknown"}", fontSize = 12.sp, color = TextTertiary)
                        }
                    }
                }
            }
        } else if (error != null) {
            Text(text = error!!, color = ErrorColor, modifier = Modifier.padding(24.dp))
        }
    }
}
