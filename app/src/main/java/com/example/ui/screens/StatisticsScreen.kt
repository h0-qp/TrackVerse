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
import com.example.network.TmdbShow
import com.example.network.TmdbEpisode
import com.example.viewmodel.WatchlistViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun StatisticsScreen(navController: NavController? = null, watchlistViewModel: WatchlistViewModel = viewModel()) {
    val watchlist by watchlistViewModel.watchlist.collectAsState()
    val seriesWatchlist = watchlist.filter { it.title == null }
    val moviesWatchlist = watchlist.filter { it.title != null }
    val watchedList by watchlistViewModel.watchedEpisodesList.collectAsState()
    var mainTabIndex by remember { mutableStateOf(0) } // 0: Series, 1: Movies
    var seriesTabIndex by remember { mutableStateOf(1) } // 0: Upcoming, 1: Watchlist

    var showFilterSortDialog by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf("Title") }
    var sortOrder by remember { mutableStateOf("Ascending") }

    val sortedSeriesWatchlist = remember(seriesWatchlist, sortBy, sortOrder) {
        var list = seriesWatchlist
        list = when (sortBy) {
            "Rating" -> list.sortedBy { it.voteAverage }
            else -> list.sortedBy { it.displayTitle }
        }
        if (sortOrder == "Descending") list = list.reversed()
        list
    }

    val sortedMoviesWatchlist = remember(moviesWatchlist, sortBy, sortOrder) {
        var list = moviesWatchlist
        list = when (sortBy) {
            "Rating" -> list.sortedBy { it.voteAverage }
            else -> list.sortedBy { it.displayTitle }
        }
        if (sortOrder == "Descending") list = list.reversed()
        list
    }

    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    LaunchedEffect(Unit) {
        watchlistViewModel.loadWatchlist() // explicit fallback execution
    }

    val upcomingEpisodes = seriesWatchlist.flatMap { show ->
        val eps = mutableListOf<Pair<TmdbShow, TmdbEpisode>>()
        show.lastEpisodeToAir?.let { 
            if (watchedList[show.id]?.contains(it.id) != true) {
                eps.add(Pair(show, it))
            }
        }
        show.nextEpisodeToAir?.let { 
            if (watchedList[show.id]?.contains(it.id) != true) {
                eps.add(Pair(show, it))
            }
        }
        eps
    }.distinctBy { it.second.id }.sortedBy { it.second.airDate }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        TabRow(
            selectedTabIndex = mainTabIndex,
            containerColor = Color.Black,
            contentColor = Color.White,
            indicator = { tabPositions ->
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[mainTabIndex])
                        .height(3.dp)
                        .background(Color.White)
                )
            }
        ) {
            Tab(
                selected = mainTabIndex == 0,
                onClick = { mainTabIndex = 0 },
                text = { Text(stringResource(R.string.series_tab), fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = mainTabIndex == 1,
                onClick = { mainTabIndex = 1 },
                text = { Text(stringResource(R.string.movies_tab), fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = mainTabIndex == 2,
                onClick = { mainTabIndex = 2 },
                text = { Text("Insights", fontWeight = FontWeight.Bold) }
            )
        }

        if (mainTabIndex == 0) {
            TabRow(
                selectedTabIndex = seriesTabIndex,
                containerColor = Color.Black,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[seriesTabIndex])
                            .height(3.dp)
                            .background(Color.White)
                    )
                }
            ) {
                Tab(
                    selected = seriesTabIndex == 0,
                    onClick = { seriesTabIndex = 0 },
                    text = { Text(stringResource(R.string.upcoming_tab), fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = seriesTabIndex == 1,
                    onClick = { seriesTabIndex = 1 },
                    text = { Text(stringResource(R.string.watchlist_tab), fontWeight = FontWeight.Bold) }
                )
            }

            if (seriesTabIndex == 1) {
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showFilterSortDialog = true }) {
                        Text("Sort: $sortBy ($sortOrder)", color = BlueHighlight)
                    }
                }
            }

            if (seriesTabIndex == 0) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val groupedEpisodes = java.util.LinkedHashMap<String, MutableList<Pair<TmdbShow, TmdbEpisode>>>()
                
                // 1. Sort chronologically
                val sortedUpcoming = upcomingEpisodes.sortedBy { it.second.airDate ?: "9999-12-31" }
                
                // 2. Exact grouping logic as requested
                for ((show, episode) in sortedUpcoming) {
                    val airDateStr = episode.airDate
                    if (airDateStr.isNullOrEmpty()) {
                        groupedEpisodes.getOrPut("later") { mutableListOf() }.add(Pair(show, episode))
                        continue
                    }
                    
                    try {
                        val parsedDate = java.time.LocalDate.parse(airDateStr)
                        val today = java.time.LocalDate.now()
                        val diffDays = java.time.temporal.ChronoUnit.DAYS.between(today, parsedDate).toInt()
                        
                        val categoryKey = when {
                            diffDays < -1 -> "past:${java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale("ar")).format(parsedDate)}"
                            diffDays == -1 -> "yesterday"
                            diffDays == 0 -> "today"
                            diffDays == 1 -> "tomorrow"
                            diffDays in 2..7 -> "weekday:${java.time.format.DateTimeFormatter.ofPattern("EEEE", java.util.Locale("ar")).format(parsedDate)}"
                            else -> "later"
                        }
                        
                        groupedEpisodes.getOrPut(categoryKey) { mutableListOf() }.add(Pair(show, episode))
                    } catch (e: Exception) {
                        groupedEpisodes.getOrPut("later") { mutableListOf() }.add(Pair(show, episode))
                    }
                }
                
                val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                val keysList = groupedEpisodes.keys.toList()
                
                androidx.compose.runtime.LaunchedEffect(keysList) {
                    var targetIndex = -1
                    var currentIndex = 0
                    for (key in keysList) {
                        if (targetIndex == -1 && (key == "today" || key == "tomorrow" || key.startsWith("weekday:") || key == "later")) {
                            targetIndex = currentIndex
                        }
                        currentIndex += 1 // for header
                        currentIndex += groupedEpisodes[key]?.size ?: 0 // for items
                    }
                    if (targetIndex > 0) {
                        listState.scrollToItem(targetIndex)
                    }
                }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                groupedEpisodes.forEach { (category, episodes) ->
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            val headerText = when {
                                category == "yesterday" -> "البارحة" // stringResource(R.string.yesterday) if translated
                                category == "today" -> "اليوم" // stringResource(R.string.today)
                                category == "tomorrow" -> "غداً" // stringResource(R.string.tomorrow)
                                category == "later" -> "لاحقاً" // stringResource(R.string.later)
                                category.startsWith("past:") -> category.removePrefix("past:")
                                category.startsWith("weekday:") -> category.removePrefix("weekday:")
                                else -> category
                            }
                            val translatedHeaderText = when (headerText) {
                                stringResource(R.string.yesterday) -> "البارحة"
                                stringResource(R.string.today) -> "اليوم"
                                stringResource(R.string.tomorrow) -> "غداً"
                                stringResource(R.string.later) -> "لاحقاً"
                                else -> headerText
                            }
                            Box(modifier = Modifier.background(Color(0xFF333333), RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 6.dp)) {
                                Text(translatedHeaderText, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    items(episodes) { (show, episode) ->
                        var diffDaysItem = 1 // default to future
                        if (!episode.airDate.isNullOrEmpty()) {
                            try {
                                val date = java.time.LocalDate.parse(episode.airDate)
                                val today = java.time.LocalDate.now()
                                diffDaysItem = java.time.temporal.ChronoUnit.DAYS.between(today, date).toInt()
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
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                                    .clip(RoundedCornerShape(8.dp)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = "https://image.tmdb.org/t/p/w200${show.backdropPath ?: show.posterPath}",
                                    contentDescription = null,
                                    modifier = Modifier.width(100.dp).fillMaxHeight(),
                                    contentScale = ContentScale.Crop
                                )
                                
                                Column(
                                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("${show.displayTitle.uppercase()} >", color = Color.White, fontSize = 10.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "S${String.format(Locale.US, "%02d", episode.seasonNumber ?: 1)} | E${String.format(Locale.US, "%02d", episode.episodeNumber ?: 1)}",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(episode.name ?: "TBA", color = Color.LightGray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (diffDaysItem <= 0) {
                                        val badgeText = if (diffDaysItem < 0) stringResource(R.string.badge_aired) else stringResource(R.string.badge_new)
                                        val badgeColor = if (diffDaysItem < 0) Color(0xFF81C784) else Color.Yellow
                                        val badgeTextColor = Color.Black
                                        
                                        Box(
                                            modifier = Modifier
                                                .background(badgeColor, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(badgeText, color = badgeTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                
                                // Status Checkbox
                                Box(modifier = Modifier.padding(16.dp)) {
                                    val isWatched = watchedList[show.id]?.contains(episode.id) == true
                                    androidx.compose.material3.IconButton(onClick = {
                                        watchlistViewModel.toggleEpisodeWatched(show.id, episode.id)
                                    }) {
                                        Box(modifier = Modifier.size(24.dp).background(if (isWatched) Color.Gray else Color.White, androidx.compose.foundation.shape.CircleShape).border(1.dp, Color.Gray, androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
                                            if (isWatched) {
                                                Icon(androidx.compose.material.icons.Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            } else {
                                                Icon(androidx.compose.material.icons.Icons.Default.Check, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                    }
                }
            }
        } else {
            val (finishedShows, activeShows) = sortedSeriesWatchlist.partition { show ->
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
                        Box(modifier = Modifier.padding(16.dp)) {
                            androidx.compose.material3.IconButton(
                                onClick = {
                                    if (episode != null) {
                                        watchlistViewModel.toggleEpisodeWatched(show.id, episode.id)
                                    }
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isWatched) Color(0xFF4CAF50) else Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Watched", tint = if (isWatched) Color.White else Color.Black)
                                }
                            }
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
                                    navController?.navigate("details/${show.id}/$isMovie?source=stats")
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
                                        state = rememberSharedContentState(key = "image-${show.id}-stats"),
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
        } // End of seriesTabIndex else block
        } else if (mainTabIndex == 1) {
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { showFilterSortDialog = true }) {
                    Text("Sort: $sortBy ($sortOrder)", color = BlueHighlight)
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(sortedMoviesWatchlist) { show ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clickable {
                                navController?.navigate("details/${show.id}/true")
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
                                show.title ?: "",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else if (mainTabIndex == 2) {
            val totalMovies = moviesWatchlist.size
            val totalSeries = seriesWatchlist.size
            val totalEpisodesWatched = watchedList.values.sumOf { it.size }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Text(androidx.compose.ui.res.stringResource(com.example.R.string.year_in_review), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BlueHighlight)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))))
                            .padding(24.dp)
                    ) {
                        Column {
                            Text("2026 " + androidx.compose.ui.res.stringResource(com.example.R.string.wrapped_title), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Movies", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                                    Text("$totalMovies", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("TV Shows", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                                    Text("$totalSeries", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text(androidx.compose.ui.res.stringResource(com.example.R.string.episodes_watched), color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                                    Text("$totalEpisodesWatched", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            val context = androidx.compose.ui.platform.LocalContext.current
                            Button(
                                onClick = {
                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_SUBJECT, "My Year in Review")
                                        putExtra(android.content.Intent.EXTRA_TEXT, "I watched $totalMovies movies and $totalEpisodesWatched episodes this year! Track your shows with our app.")
                                    }
                                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share via"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF4A00E0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(androidx.compose.ui.res.stringResource(com.example.R.string.share), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                
                item {
                    Text(androidx.compose.ui.res.stringResource(com.example.R.string.genre_preferences), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val genres = listOf("Sci-Fi" to 0.8f, "Action" to 0.6f, "Drama" to 0.4f, "Comedy" to 0.3f)
                    genres.forEach { (genre, ratio) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(genre, color = TextSecondary, modifier = Modifier.width(60.dp))
                            Box(modifier = Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(6.dp)).background(SurfaceDark)) {
                                Box(modifier = Modifier.fillMaxWidth(ratio).fillMaxHeight().clip(RoundedCornerShape(6.dp)).background(BlueHighlight))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterSortDialog) {
        @OptIn(ExperimentalMaterial3Api::class)
        ModalBottomSheet(
            onDismissRequest = { showFilterSortDialog = false },
            containerColor = SurfaceDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Sort By", color = TextPrimary, fontWeight = FontWeight.Bold)
                Row {
                    RadioButton(selected = sortBy == "Title", onClick = { sortBy = "Title" })
                    Text("Title", color = TextPrimary, modifier = Modifier.align(Alignment.CenterVertically).padding(end = 16.dp))
                    RadioButton(selected = sortBy == "Rating", onClick = { sortBy = "Rating" })
                    Text("Rating", color = TextPrimary, modifier = Modifier.align(Alignment.CenterVertically))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Sort Order", color = TextPrimary, fontWeight = FontWeight.Bold)
                Row {
                    RadioButton(selected = sortOrder == "Ascending", onClick = { sortOrder = "Ascending" })
                    Text("Ascending", color = TextPrimary, modifier = Modifier.align(Alignment.CenterVertically).padding(end = 16.dp))
                    RadioButton(selected = sortOrder == "Descending", onClick = { sortOrder = "Descending" })
                    Text("Descending", color = TextPrimary, modifier = Modifier.align(Alignment.CenterVertically))
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showFilterSortDialog = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BlueHighlight)
                ) {
                    Text("Apply", color = Color.White)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
