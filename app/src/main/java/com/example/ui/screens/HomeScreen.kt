package com.example.ui.screens

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.tween
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
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.getValue
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
import com.example.R
import com.example.network.TmdbShow
import com.example.ui.theme.*
import com.example.viewmodel.HomeViewModel

import com.example.viewmodel.SearchViewModel
import com.example.ui.screens.SearchResultItem
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.res.stringResource
import com.example.viewmodel.WatchlistViewModel

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.delay

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = viewModel(), watchlistViewModel: WatchlistViewModel = viewModel()) {
    val authViewModel: com.example.viewmodel.AuthViewModel = viewModel()
    val user by authViewModel.user.collectAsState()
    val trendingShows by viewModel.trendingShows.collectAsState()
    val topRatedShows by viewModel.topRatedShows.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
    
    val watchlist by watchlistViewModel.watchlist.collectAsState()
    val watchedCounts by watchlistViewModel.watchedEpisodesCount.collectAsState()

    LaunchedEffect(user) {
        watchlistViewModel.loadWatchlist() // Explicit generic network fetch call 
    }
    
    val totalShows = watchlist.count { it.title == null }.toString()
    val totalMovies = watchlist.count { it.name == null }.toString()
    val totalWatchedEpisodes = watchedCounts.values.sum().toString()

    val searchViewModel: SearchViewModel = viewModel()
    val query by searchViewModel.query.collectAsState()
    val searchResults by searchViewModel.searchResults.collectAsState()
    val isSearching by searchViewModel.isLoading.collectAsState()
    
    val recommendationsViewModel: com.example.viewmodel.RecommendationsViewModel = viewModel()
    val aiRecommendations by recommendationsViewModel.recommendations.collectAsState()
    val isRecsLoading by recommendationsViewModel.isLoading.collectAsState()

    LaunchedEffect(watchlist, watchedCounts) {
        val watchedShows = watchlist.filter { show -> (watchedCounts[show.id] ?: 0) > 0 }
        if (watchedShows.isNotEmpty() || watchlist.isNotEmpty()) {
            recommendationsViewModel.fetchRecommendations(watchedShows = watchedShows, watchlist = watchlist)
        }
    }

    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = { viewModel.fetchHomeData() },
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    text = androidx.compose.ui.res.stringResource(id = R.string.app_name).uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueHighlight,
                    letterSpacing = 2.sp
                )
                if (user == null || user?.isAnonymous == true) {
                    Text(
                        text = stringResource(R.string.guest_user),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            // Profile image
            val profileImg = user?.photoUrl?.toString() ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=${user?.displayName ?: "User"}"
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SurfaceDark)
                    .border(1.dp, BorderStroke, CircleShape)
            ) {
                AsyncImage(
                    model = profileImg,
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

        // Search Bar
        TextField(
            value = query,
            onValueChange = { searchViewModel.onQueryChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, BorderStroke, RoundedCornerShape(16.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark,
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            placeholder = { Text("Movies, Shows, etc...", color = TextTertiary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextTertiary) }
        )
        
        if (query.isNotEmpty()) {
            if (isSearching) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BlueHighlight)
                }
            } else if (searchResults.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No results found.", color = TextTertiary, fontSize = 16.sp)
                }
            } else {
                // Search Results Grid (we use chunked to simulate grid in verticalScroll)
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    val chunks = searchResults.chunked(3)
                    chunks.forEach { rowItems ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            rowItems.forEach { show ->
                                SearchResultItem(
                                    show = show,
                                    sourceKey = "search",
                                    modifier = Modifier.weight(1f).clickable {
                                        val isMovie = show.title != null
                                        navController.navigate("details/${show.id}/$isMovie?source=search")
                                    }
                                )
                            }
                            // Fill remaining space if chunk is less than 3
                            repeat(3 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        } else {
        
        // Genre Chips
        val genres = listOf("Action", "Drama", "Sci-Fi", "Comedy", "Anime", "Thriller")
        val context = androidx.compose.ui.platform.LocalContext.current
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(genres) { genre ->
                Box(
                    modifier = Modifier
                        .background(SurfaceDark, CircleShape)
                        .border(1.dp, BorderStroke, CircleShape)
                        .clickable { 
                            android.widget.Toast.makeText(context, "Filtering by $genre coming soon!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(genre, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Continue Watching Section
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(stringResource(R.string.trending_today), fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(stringResource(R.string.view_all), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = BlueHighlight)
        }

        // Hero Card Carousel
        val heroShows = trendingShows.take(5)
        if (heroShows.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { heroShows.size })
            
            LaunchedEffect(pagerState) {
                while (true) {
                    delay(4000)
                    val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
                    pagerState.animateScrollToPage(nextPage)
                }
            }
            
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .padding(bottom = 32.dp)
            ) { page ->
                val heroShow = heroShows[page]
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceDark)
                ) {
                    var imgModifier = Modifier.fillMaxSize().clickable {
                        val isMovie = heroShow.title != null
                        navController.navigate("details/${heroShow.id}/$isMovie?source=home-hero-${page}")
                    }
                    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                        with(sharedTransitionScope) {
                            imgModifier = imgModifier.sharedElement(
                                state = rememberSharedContentState(key = "image-${heroShow.id}-home-hero-${page}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = { _, _ -> tween(durationMillis = 500) }
                            )
                        }
                    }

                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w780${heroShow.backdropPath ?: heroShow.posterPath}",
                        contentDescription = heroShow.displayTitle,
                        modifier = imgModifier,
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
                                Text("★ ${heroShow.voteAverage?.toString()?.take(3) ?: stringResource(R.string.not_available)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFFC107))
                                Text(heroShow.displayTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(stringResource(R.string.trending).uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Releasing Today Section
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val releasingToday = watchlist.filter { show -> show.nextEpisodeToAir?.airDate == today }
        
        if (releasingToday.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.releasing_today), fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            }
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(releasingToday) { show ->
                    AiRecItem(
                        show = show,
                        watchlistViewModel = watchlistViewModel,
                        modifier = Modifier.width(120.dp).clickable {
                            navController.navigate("details/${show.id}/false?source=home-releasing")
                        }
                    )
                }
            }
        }
        
        // Continue Watching
        val continueWatching = watchlist.filter { show ->
            val watched = watchedCounts[show.id] ?: 0
            val total = show.numberOfEpisodes ?: 0
            total > 0 && watched > 0 && watched < total
        }
        
        if (continueWatching.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.continue_watching), fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            }
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(continueWatching) { show ->
                    Column(modifier = Modifier.width(150.dp).clickable { navController.navigate("details/${show.id}/false") }) {
                        val watched = watchedCounts[show.id] ?: 0
                        val total = show.numberOfEpisodes ?: 1
                        val progress = watched.toFloat() / total.toFloat()
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f/9f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceDark)
                        ) {
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w300${show.backdropPath ?: show.posterPath}",
                                contentDescription = show.displayTitle,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(show.displayTitle, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(stringResource(R.string.episodes_progress, watched, total), color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = BlueHighlight,
                            trackColor = SurfaceDark
                        )
                    }
                }
            }
        }

        // AI Personalized Recommendations
        if (aiRecommendations.isNotEmpty() || isRecsLoading) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("✨ Personalized For You", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            }
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isRecsLoading) {
                    items(3) {
                        ShimmerAiRecItem(modifier = Modifier.width(120.dp))
                    }
                } else {
                    items(aiRecommendations) { show ->
                        AiRecItem(
                            show = show,
                            watchlistViewModel = watchlistViewModel,
                            modifier = Modifier.width(120.dp).clickable {
                                val isMovie = show.title != null
                                navController.navigate("details/${show.id}/$isMovie?source=home-recs")
                            }
                        )
                    }
                }
            }
        }

        // Top Rated Shows
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.top_rated_shows), fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }

        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (topRatedShows.isNotEmpty()) {
                val displays = topRatedShows.take(6)
                displays.forEach { show ->
                    AiRecItem(
                        show = show,
                        watchlistViewModel = watchlistViewModel,
                        modifier = Modifier.widthIn(min = 100.dp, max = 150.dp).weight(1f).clickable {
                            val isMovie = show.title != null
                            navController.navigate("details/${show.id}/$isMovie?source=home-grid")
                        }
                    )
                }
            } else if (isLoading) {
                 repeat(3) {
                     ShimmerAiRecItem(modifier = Modifier.widthIn(min = 100.dp, max = 150.dp).weight(1f))
                 }
            } else if (error != null) {
                 Text("Error: $error", color = ErrorColor, fontSize = 12.sp)
            }
        }
        
        val topRatedMovies by viewModel.topRatedMovies.collectAsState()
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.top_rated_movies), fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }

        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (topRatedMovies.isNotEmpty()) {
                val displays = topRatedMovies.take(6)
                displays.forEach { show ->
                    AiRecItem(
                        show = show,
                        watchlistViewModel = watchlistViewModel,
                        modifier = Modifier.widthIn(min = 100.dp, max = 150.dp).weight(1f).clickable {
                            val isMovie = show.title != null
                            navController.navigate("details/${show.id}/$isMovie?source=home-grid-movies")
                        }
                    )
                }
            } else if (isLoading) {
                 repeat(3) {
                     ShimmerAiRecItem(modifier = Modifier.widthIn(min = 100.dp, max = 150.dp).weight(1f))
                 }
            } else if (error != null) {
                 Text("Error: $error", color = ErrorColor, fontSize = 12.sp)
            }
        }
        } // Close else for query.isNotEmpty()
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
fun ShimmerAiRecItem(modifier: Modifier = Modifier) {
    val transition = androidx.compose.animation.core.rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        )
    )
    val brush = Brush.linearGradient(
        colors = listOf(SurfaceDark, Color.LightGray.copy(alpha = 0.2f), SurfaceDark),
        start = androidx.compose.ui.geometry.Offset(10f, 10f),
        end = androidx.compose.ui.geometry.Offset(translateAnim, translateAnim)
    )

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(16.dp))
                .background(brush)
        )
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(0.7f)
                .height(14.dp)
                .background(brush, RoundedCornerShape(4.dp))
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AiRecItem(show: TmdbShow, watchlistViewModel: WatchlistViewModel, modifier: Modifier = Modifier) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    val watchlist by watchlistViewModel.watchlist.collectAsState()
    val isInWatchlist = watchlist.any { it.id == show.id }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .border(1.dp, BorderStroke, RoundedCornerShape(16.dp))
        ) {
            var imgModifier = Modifier.fillMaxSize()
            if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                with(sharedTransitionScope) {
                    imgModifier = imgModifier.sharedElement(
                        state = rememberSharedContentState(key = "image-${show.id}-home-grid"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ -> tween(durationMillis = 500) }
                    )
                }
            }

            AsyncImage(
                model = "https://image.tmdb.org/t/p/w342${show.posterPath}",
                contentDescription = show.displayTitle,
                modifier = imgModifier,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                colorFilter = androidx.compose.ui.graphics.ColorFilter.colorMatrix(androidx.compose.ui.graphics.ColorMatrix().apply { setToSaturation(0.2f) })
            )
            
            // Quick Add Button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .background(if (isInWatchlist) BlueHighlight else Color.Black.copy(alpha = 0.5f), CircleShape)
                    .clickable {
                        if (isInWatchlist) {
                            watchlistViewModel.removeFromWatchlist(show.id)
                        } else {
                            watchlistViewModel.addToWatchlist(show, isTracking = true)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(if (isInWatchlist) "✓" else "+", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Text(
            text = show.displayTitle,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SearchResultItem(show: TmdbShow, sourceKey: String, modifier: Modifier = Modifier) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceDark)
                .border(1.dp, BorderStroke, RoundedCornerShape(12.dp))
        ) {
            var imgModifier = Modifier.fillMaxSize()
            if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                with(sharedTransitionScope) {
                    imgModifier = imgModifier.sharedElement(
                        state = rememberSharedContentState(key = "image-${show.id}-$sourceKey"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ -> tween(durationMillis = 500) }
                    )
                }
            }

            AsyncImage(
                model = "https://image.tmdb.org/t/p/w342${show.posterPath}",
                contentDescription = show.displayTitle,
                modifier = imgModifier,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }
        Text(
            text = show.displayTitle,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        Text(
            text = "★ ${show.voteAverage ?: "N/A"}",
            fontSize = 10.sp,
            color = Color(0xFFFFC107),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
