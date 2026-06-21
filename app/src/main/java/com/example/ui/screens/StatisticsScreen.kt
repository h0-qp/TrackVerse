package com.example.ui.screens

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.*
import com.example.viewmodel.WatchlistViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun StatisticsScreen(navController: NavController? = null, watchlistViewModel: WatchlistViewModel = viewModel()) {
    val watchlist by watchlistViewModel.watchlist.collectAsState()
    val watchedList by watchlistViewModel.watchedEpisodesList.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(1) } // 0: Upcoming, 1: Watchlist

    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    LaunchedEffect(Unit) {
        watchlistViewModel.loadWatchlist()
    }

    val upcomingEpisodes = watchlist.mapNotNull { show ->
        show.nextEpisodeToAir?.let { episode ->
            Pair(show, episode)
        }
    }.sortedBy { it.second.airDate }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Black,
            contentColor = Color.White,
            indicator = { tabPositions ->
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .height(3.dp)
                        .background(Color.White)
                )
            }
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text(stringResource(R.string.upcoming_tab), fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text(stringResource(R.string.watchlist_tab), fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedTabIndex == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(upcomingEpisodes) { (show, episode) ->
                    var daysUntil = -1
                    var hoursUntil = -1
                    if (!episode.airDate.isNullOrEmpty()) {
                        try {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val date = sdf.parse(episode.airDate)
                            if (date != null) {
                                val diff = date.time - System.currentTimeMillis()
                                if (diff > 0) {
                                    daysUntil = (diff / (1000 * 60 * 60 * 24)).toInt()
                                    hoursUntil = (diff / (1000 * 60 * 60)).toInt() % 24
                                }
                            }
                        } catch (e: Exception) {}
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clickable {
                                val isMovie = show.title != null
                                navController?.navigate("details/${show.id}/$isMovie")
                            }
                            .padding(8.dp)
                            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = "https://image.tmdb.org/t/p/w200${show.posterPath ?: show.backdropPath}",
                            contentDescription = null,
                            modifier = Modifier.width(90.dp).fillMaxHeight(),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("< ${show.displayTitle.uppercase()}", color = Color.White, fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "S${String.format("%02d", episode.seasonNumber ?: 1)} | E${String.format("%02d", episode.episodeNumber ?: 1)}",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(episode.name ?: "TBA", color = Color.LightGray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            if (daysUntil >= 0) {
                                val timeText = if (daysUntil == 0 && hoursUntil >= 0) {
                                    String.format(stringResource(R.string.in_hours), hoursUntil)
                                } else {
                                    String.format(stringResource(R.string.in_days), daysUntil)
                                }
                                Text(
                                    text = timeText,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            if (daysUntil <= 1) {
                                Box(
                                    modifier = Modifier
                                        .background(if (daysUntil == 0) Color.Yellow else Color.White, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("New", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            val (finishedShows, activeShows) = watchlist.partition { show ->
                val watchedCount = watchedList[show.id]?.size ?: 0
                val totalEpisodes = show.numberOfEpisodes ?: 0
                totalEpisodes > 0 && watchedCount >= totalEpisodes
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(activeShows) { show ->
                    val episode = show.nextEpisodeToAir
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clickable {
                                val isMovie = show.title != null
                                navController?.navigate("details/${show.id}/$isMovie")
                            }
                            .padding(8.dp)
                            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = "https://image.tmdb.org/t/p/w200${show.posterPath ?: show.backdropPath}",
                            contentDescription = null,
                            modifier = Modifier.width(90.dp).fillMaxHeight(),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("< ${show.displayTitle.uppercase()}", color = Color.White, fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val seasonStr = episode?.seasonNumber?.let { String.format("%02d", it) } ?: "01"
                            val epStr = episode?.episodeNumber?.let { String.format("%02d", it) } ?: "01"
                            Text(
                                "S$seasonStr | E$epStr",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(episode?.name ?: show.displayTitle, color = Color.LightGray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        val isWatched = episode?.id?.let { epId -> watchedList[show.id]?.contains(epId) } == true
                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isWatched) Color(0xFF4CAF50) else Color.White)
                                .clickable {
                                    if (episode != null) {
                                        watchlistViewModel.toggleEpisodeWatched(show.id, episode.id)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Watched", tint = if (isWatched) Color.White else Color.Black)
                        }
                    }
                }

                if (finishedShows.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.finished_shows),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
                        )
                    }
                    items(finishedShows) { show ->
                        val episode = show.nextEpisodeToAir
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clickable {
                                    val isMovie = show.title != null
                                    navController?.navigate("details/${show.id}/$isMovie")
                                }
                                .padding(8.dp)
                                .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var imgModifier = Modifier.width(90.dp).fillMaxHeight()
                            if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                                with(sharedTransitionScope) {
                                    imgModifier = imgModifier.sharedElement(
                                        state = rememberSharedContentState(key = "image-${show.id}"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        boundsTransform = { _, _ -> tween(durationMillis = 500) }
                                    )
                                }
                            }

                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w200${show.posterPath ?: show.backdropPath}",
                                contentDescription = null,
                                modifier = imgModifier,
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("< ${show.displayTitle.uppercase()}", color = Color.White, fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    stringResource(R.string.finished_shows),
                                    color = Color(0xFF4CAF50),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(show.overview ?: "", color = Color.LightGray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
    }
}
