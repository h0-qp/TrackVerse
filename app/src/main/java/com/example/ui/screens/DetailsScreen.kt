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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ui.theme.*
import com.example.viewmodel.DetailsViewModel
import com.example.viewmodel.WatchlistViewModel
import com.example.viewmodel.ReviewViewModel
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DetailsScreen(
    showId: Int,
    isMovie: Boolean,
    onBack: () -> Unit,
    onPersonClick: (Int) -> Unit = {},
    detailsViewModel: DetailsViewModel = viewModel(),
    watchlistViewModel: WatchlistViewModel = viewModel(),
    reviewViewModel: ReviewViewModel = viewModel()
) {
    val show by detailsViewModel.show.collectAsState()
    val isLoading by detailsViewModel.isLoading.collectAsState()
    val error by detailsViewModel.error.collectAsState()
    val watchlist by watchlistViewModel.watchlist.collectAsState()
    
    val reviews by reviewViewModel.reviews.collectAsState()
    val averageRating by reviewViewModel.averageRating.collectAsState()

    val scrollState = rememberScrollState()

    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    LaunchedEffect(showId) {
        detailsViewModel.loadDetails(showId, isMovie)
        watchlistViewModel.loadWatchlist()
        reviewViewModel.loadReviews(showId, isMovie)
    }

    val isInWatchlist = watchlist.any { it.id == show?.id }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(550.dp)
                    .graphicsLayer {
                        translationY = scrollState.value * 0.5f // Parallax effect
                        alpha = 1f - (scrollState.value / 600f) // Fade out as it goes up
                    }
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(SurfaceDark)
            ) {
                var imgModifier = Modifier.fillMaxSize()
                if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                    with(sharedTransitionScope) {
                        imgModifier = imgModifier.sharedElement(
                            state = rememberSharedContentState(key = "image-$showId"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ -> tween(durationMillis = 500) }
                        )
                    }
                }

                AsyncImage(
                    model = "https://image.tmdb.org/t/p/w780${show?.posterPath}",
                    contentDescription = show?.displayTitle,
                    modifier = imgModifier,
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
            Column(modifier = Modifier.padding(24.dp).background(MaterialTheme.colorScheme.background)) {
                Text(
                    text = show?.displayTitle ?: "Unknown",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "★ ${show?.voteAverage?.toString()?.take(3) ?: androidx.compose.ui.res.stringResource(com.example.R.string.not_available)}",
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
                Text(androidx.compose.ui.res.stringResource(com.example.R.string.overview), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = show?.overview ?: "No overview available.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 22.sp
                )

                if (!show?.credits?.cast.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(androidx.compose.ui.res.stringResource(com.example.R.string.cast), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(show?.credits?.cast?.take(15) ?: emptyList()) { actor ->
                            Column(
                                modifier = Modifier
                                    .width(80.dp)
                                    .clickable { onPersonClick(actor.id) },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = "https://image.tmdb.org/t/p/w185${actor.profilePath}",
                                    contentDescription = actor.name,
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(SurfaceDark),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = actor.name,
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    fontWeight = FontWeight.Medium,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = actor.character ?: "",
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                val trackedItem = watchlist.find { it.id == show?.id }
                val isInWatchlistCheck = trackedItem != null
                val watchedCount = watchlistViewModel.watchedEpisodesCount.collectAsState().value[show?.id] ?: 0
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                val context = androidx.compose.ui.platform.LocalContext.current
                
                var playAnimation by remember { mutableStateOf(false) }
                val sizeScale by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (playAnimation) 1.2f else 1f,
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                    ),
                    finishedListener = { playAnimation = false }
                )

                Button(
                    onClick = {
                        if (auth.currentUser == null) {
                            android.widget.Toast.makeText(context, "Please sign in from Profile to subscribe", android.widget.Toast.LENGTH_LONG).show()
                        } else {
                            if (isInWatchlistCheck) {
                                watchlistViewModel.removeFromWatchlist(show!!.id)
                            } else {
                                playAnimation = true
                                watchlistViewModel.addToWatchlist(show!!, isTracking = true)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isInWatchlistCheck) SurfaceDark else BlueHighlight,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp).graphicsLayer(scaleX = sizeScale, scaleY = sizeScale)
                ) {
                    Text(
                        if (isInWatchlistCheck) androidx.compose.ui.res.stringResource(com.example.R.string.unsubscribe_remove) else androidx.compose.ui.res.stringResource(com.example.R.string.subscribe_series),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                if (!isMovie && show?.seasons != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(androidx.compose.ui.res.stringResource(com.example.R.string.seasons_and_episodes), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val seasonDetailsMap by detailsViewModel.seasonDetails.collectAsState()
                    val watchedEpisodeIds = watchlistViewModel.watchedEpisodesList.collectAsState().value[show?.id] ?: emptyList()
                    val today = java.util.Date()
                    
                    show?.seasons?.filter { (it.seasonNumber ?: 0) > 0 }?.forEach { season ->
                        var isExpanded by remember { mutableStateOf(false) }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(SurfaceDark, RoundedCornerShape(12.dp))
                                .border(1.dp, BorderStroke, RoundedCornerShape(12.dp))
                                .clickable {
                                    isExpanded = !isExpanded
                                    if (isExpanded) {
                                        detailsViewModel.loadSeasonDetails(show!!.id, season.seasonNumber ?: 1)
                                    }
                                }
                                .padding(16.dp)
                        ) {
                            Text("${androidx.compose.ui.res.stringResource(com.example.R.string.season)} ${season.seasonNumber}: ${season.name ?: ""}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            
                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(16.dp))
                                val episodes = seasonDetailsMap[season.seasonNumber]
                                if (episodes != null) {
                                    episodes.forEach { episode ->
                                        val isWatched = watchedEpisodeIds.contains(episode.id)
                                        val airDateStr = episode.airDate
                                        var daysUntil = -1
                                        var hoursUntil = -1
                                        if (airDateStr?.isNotEmpty() == true) {
                                            try {
                                                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                                val date = sdf.parse(airDateStr)
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
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${episode.episodeNumber}",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextTertiary,
                                                modifier = Modifier.width(32.dp)
                                            )
                                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                                Text(episode.name ?: androidx.compose.ui.res.stringResource(com.example.R.string.tba), fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                                Text(airDateStr ?: androidx.compose.ui.res.stringResource(com.example.R.string.unknown_date), fontSize = 12.sp, color = TextTertiary)
                                            }
                                            
                                            if (daysUntil >= 0) {
                                                val timeText = if (daysUntil == 0 && hoursUntil >= 0) {
                                                    String.format(androidx.compose.ui.res.stringResource(com.example.R.string.in_hours), hoursUntil)
                                                } else {
                                                    String.format(androidx.compose.ui.res.stringResource(com.example.R.string.in_days), daysUntil)
                                                }
                                                Text(
                                                    text = timeText,
                                                    fontSize = 12.sp,
                                                    color = BlueHighlight,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                    modifier = Modifier.background(BlueHighlight.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            } else {
                                                Checkbox(
                                                    checked = isWatched,
                                                    onCheckedChange = {
                                                        if (auth.currentUser == null) {
                                                            android.widget.Toast.makeText(context, "Please sign in to track progress", android.widget.Toast.LENGTH_SHORT).show()
                                                        } else if (!isInWatchlist) {
                                                            android.widget.Toast.makeText(context, "Please subscribe to the series first", android.widget.Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            watchlistViewModel.toggleEpisodeWatched(show!!.id, episode.id)
                                                        }
                                                    },
                                                    colors = CheckboxDefaults.colors(
                                                        checkedColor = BlueHighlight,
                                                        uncheckedColor = TextTertiary,
                                                        checkmarkColor = BgDark
                                                    )
                                                )
                                            }
                                        }
                                        HorizontalDivider(color = BorderStroke)
                                    }
                                } else {
                                    CircularProgressIndicator(color = BlueHighlight, modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally))
                                }
                            }
                        }
                    }
                }

                if (!isMovie && show?.nextEpisodeToAir != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(androidx.compose.ui.res.stringResource(com.example.R.string.next_episode), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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
                            Text(show?.nextEpisodeToAir?.name ?: androidx.compose.ui.res.stringResource(com.example.R.string.tba), fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            Text(String.format(androidx.compose.ui.res.stringResource(com.example.R.string.airs), show?.nextEpisodeToAir?.airDate ?: androidx.compose.ui.res.stringResource(com.example.R.string.unknown_date)), fontSize = 12.sp, color = TextTertiary)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                Text(androidx.compose.ui.res.stringResource(com.example.R.string.reviews), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(16.dp))

                var userRating by remember { mutableStateOf(0f) }
                var userReviewText by remember { mutableStateOf("") }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Your Rating", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            for (i in 1..5) {
                                Icon(
                                    imageVector = if (i <= userRating) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = "Star $i",
                                    tint = if (i <= userRating) Color(0xFFFFD700) else TextTertiary,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { userRating = i.toFloat() }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = userReviewText,
                            onValueChange = { userReviewText = it },
                            placeholder = { Text(androidx.compose.ui.res.stringResource(com.example.R.string.write_review), color = TextTertiary) },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BlueHighlight,
                                unfocusedBorderColor = BorderStroke,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = BlueHighlight
                            ),
                            maxLines = 4
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (auth.currentUser == null) {
                                    android.widget.Toast.makeText(context, "Please sign in to write a review", android.widget.Toast.LENGTH_SHORT).show()
                                } else if (userRating > 0f) {
                                    reviewViewModel.submitReview(showId, isMovie, userRating, userReviewText)
                                    userReviewText = ""
                                    userRating = 0f
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BlueHighlight),
                            shape = RoundedCornerShape(8.dp),
                            enabled = userRating > 0f
                        ) {
                            Text(androidx.compose.ui.res.stringResource(com.example.R.string.submit_review), color = Color.White)
                        }
                    }
                }
                
                if (reviews.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                        Text("Average Rating:", color = TextSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                        Text(String.format("%.1f", averageRating), color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(" (${reviews.size})", color = TextTertiary, fontSize = 14.sp)
                    }
                }
                
                reviews.forEach { review ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .background(SurfaceDark, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(32.dp).clip(androidx.compose.foundation.shape.CircleShape).background(BlueHighlight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(review.userName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(review.userName, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                                    Text(review.rating.toString(), color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                        if (review.text.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(review.text, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
                        }
                    }
                }
            }
        } else if (error != null) {
            Text(text = error!!, color = ErrorColor, modifier = Modifier.padding(24.dp))
        }
        
        } // Close Column

        // Sticky Back Button overlaying the screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha=0.6f), Color.Transparent)
                    )
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .padding(top = 48.dp, start = 24.dp)
                    .size(32.dp)
                    .clickable { onBack() }
            )
        }
    }
}
