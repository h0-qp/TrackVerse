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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.example.viewmodel.SocialViewModel
import com.example.viewmodel.CustomListViewModel
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Send
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DetailsScreen(
    showId: Int,
    isMovie: Boolean,
    sourceKey: String = "",
    onBack: () -> Unit,
    onPersonClick: (Int) -> Unit = {},
    detailsViewModel: DetailsViewModel = viewModel(),
    watchlistViewModel: WatchlistViewModel = viewModel(),
    reviewViewModel: ReviewViewModel = viewModel(),
    socialViewModel: SocialViewModel = viewModel(),
    customListViewModel: CustomListViewModel = viewModel()
) {
    val show by detailsViewModel.show.collectAsState()
    val isLoading by detailsViewModel.isLoading.collectAsState()
    val error by detailsViewModel.error.collectAsState()
    val watchlist by watchlistViewModel.watchlist.collectAsState()
    
    val reviews by reviewViewModel.reviews.collectAsState()
    val userReview by reviewViewModel.userReview.collectAsState()
    val averageRating by reviewViewModel.averageRating.collectAsState()

    val myCustomLists by customListViewModel.myLists.collectAsState()
    var showAddToListDialog by remember { mutableStateOf(false) }
    var showSuggestDialog by remember { mutableStateOf(false) }
    val followingProfiles by socialViewModel.followingProfiles.collectAsState()
    var selectedEpisode by remember { mutableStateOf<com.example.network.TmdbEpisode?>(null) }

    val scrollState = rememberScrollState()

    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
    
    var dominantColor by remember { mutableStateOf<Color?>(null) }
    val dynamicThemeColor = dominantColor ?: BlueHighlight

    LaunchedEffect(showId) {
        detailsViewModel.loadDetails(showId, isMovie)
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
                    val finalKey = if (sourceKey.isNotEmpty()) "image-$showId-$sourceKey" else "image-$showId"
                    with(sharedTransitionScope) {
                        imgModifier = imgModifier.sharedElement(
                            state = rememberSharedContentState(key = finalKey),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ -> tween(durationMillis = 500) }
                        )
                    }
                }

                AsyncImage(
                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data("https://image.tmdb.org/t/p/w780${show?.posterPath}")
                        .allowHardware(false)
                        .build(),
                    contentDescription = show?.displayTitle,
                    modifier = imgModifier,
                    contentScale = ContentScale.Crop,
                    onSuccess = { result ->
                        val bitmap = (result.result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                        if (bitmap != null) {
                            androidx.palette.graphics.Palette.from(bitmap).generate { palette ->
                                palette?.dominantSwatch?.rgb?.let { colorInt ->
                                    dominantColor = Color(colorInt)
                                } ?: palette?.mutedSwatch?.rgb?.let { colorInt ->
                                    dominantColor = Color(colorInt)
                                }
                            }
                        }
                    }
                )
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha=0.6f),
                                Color.Transparent,
                                dominantColor?.copy(alpha=0.4f) ?: Color.Black.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            // Back button and Top Bar actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 24.dp, end = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onBack() }
                )
                
                val context = androidx.compose.ui.platform.LocalContext.current
                val shareTitle = show?.displayTitle ?: "this"
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAdd,
                        contentDescription = "Add to Custom List",
                        tint = Color.White,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable {
                                if (myCustomLists.isNotEmpty()) {
                                    showAddToListDialog = true
                                } else {
                                    android.widget.Toast.makeText(context, "Create a Custom List first from your Profile", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                    )
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Send,
                        contentDescription = "Suggest to Friend",
                        tint = Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                if (followingProfiles.isNotEmpty()) {
                                    showSuggestDialog = true
                                } else {
                                    android.widget.Toast.makeText(context, "Follow some friends first", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                    )
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, shareTitle)
                                    val typeString = if (isMovie) "movie" else "tv"
                                    putExtra(android.content.Intent.EXTRA_TEXT, "Check out $shareTitle on TMDB! https://www.themoviedb.org/$typeString/${show?.id}")
                                }
                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share via"))
                            }
                    )
                }
            }

                if (isLoading) {
                CircularProgressIndicator(
                    color = dynamicThemeColor,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        if (showAddToListDialog && show != null) {
            val context = androidx.compose.ui.platform.LocalContext.current
            AlertDialog(
                onDismissRequest = { showAddToListDialog = false },
                title = { Text("Add to Custom List", color = TextPrimary) },
                text = {
                    Column {
                        myCustomLists.forEach { list ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        customListViewModel.addShowToList(list.id, show!!, isMovie)
                                        android.widget.Toast.makeText(context, "Added to ${list.name}", android.widget.Toast.LENGTH_SHORT).show()
                                        showAddToListDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = dynamicThemeColor)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(list.name, color = TextPrimary, fontSize = 16.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAddToListDialog = false }) {
                        Text("Close", color = TextSecondary)
                    }
                },
                containerColor = SurfaceDark,
                textContentColor = TextSecondary
            )
        }

        if (showSuggestDialog && show != null) {
            val context = androidx.compose.ui.platform.LocalContext.current
            AlertDialog(
                onDismissRequest = { showSuggestDialog = false },
                title = { Text("Suggest to Friend", color = TextPrimary) },
                text = {
                    Column {
                        followingProfiles.forEach { friend ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        socialViewModel.suggestShow(show!!.id, show!!.displayTitle, friend.uid, friend.displayName)
                                        android.widget.Toast.makeText(context, "Suggested to ${friend.displayName}", android.widget.Toast.LENGTH_SHORT).show()
                                        showSuggestDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFF555555)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(friend.displayName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(friend.displayName, color = TextPrimary, fontSize = 16.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSuggestDialog = false }) {
                        Text("Close", color = TextSecondary)
                    }
                },
                containerColor = SurfaceDark,
                textContentColor = TextSecondary
            )
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
                        color = dynamicThemeColor
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

                if (!show?.overview.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(androidx.compose.ui.res.stringResource(com.example.R.string.overview), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = show?.overview ?: "",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 22.sp
                    )
                }

                val firstTrailer = show?.videos?.results?.firstOrNull { it.site == "YouTube" && it.type == "Trailer" }
                if (firstTrailer != null) {
                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            uriHandler.openUri("https://www.youtube.com/watch?v=${firstTrailer.key}")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = dynamicThemeColor.copy(alpha = 0.2f), contentColor = dynamicThemeColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        androidx.compose.material3.Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play Trailer", modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(androidx.compose.ui.res.stringResource(com.example.R.string.watch_trailer) ?: "Watch Trailer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

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
                var showReviewDialog by remember { mutableStateOf(false) }
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
                                if (isMovie) {
                                    showReviewDialog = true
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isInWatchlistCheck) SurfaceDark else dynamicThemeColor,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp).graphicsLayer(scaleX = sizeScale, scaleY = sizeScale)
                ) {
                    val textRes = if (isMovie) {
                        if (isInWatchlistCheck) com.example.R.string.remove_watched_movie else com.example.R.string.watch_movie
                    } else {
                        if (isInWatchlistCheck) com.example.R.string.unsubscribe_remove else com.example.R.string.subscribe_series
                    }
                    Text(
                        androidx.compose.ui.res.stringResource(textRes),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                if (showReviewDialog) {
                    var dialogRating by remember { mutableStateOf(userReview?.rating ?: 0f) }
                    var dialogText by remember { mutableStateOf(userReview?.text ?: "") }
                    AlertDialog(
                        onDismissRequest = { showReviewDialog = false },
                        title = { Text("Write a Review (Optional)", color = TextPrimary) },
                        text = {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    for (i in 1..5) {
                                        val icon = if (i <= dialogRating) Icons.Filled.Star else Icons.Outlined.Star
                                        val tint = if (i <= dialogRating) Color(0xFFFFD700) else TextTertiary
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = "Rate $i",
                                            tint = tint,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clickable { dialogRating = i.toFloat() }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = dialogText,
                                    onValueChange = { dialogText = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("What did you think?", color = TextSecondary) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = dynamicThemeColor,
                                        unfocusedBorderColor = TextTertiary,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        cursorColor = dynamicThemeColor
                                    ),
                                    maxLines = 4
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    if (dialogRating > 0f) {
                                        reviewViewModel.submitReview(showId, isMovie, dialogRating, dialogText)
                                        socialViewModel.publishActivity("REVIEWED", showId, show?.title ?: show?.name ?: "Unknown", "Rated $dialogRating/5: $dialogText")
                                    }
                                    showReviewDialog = false
                                },
                                content = { Text("Submit", color = dynamicThemeColor) }
                            )
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showReviewDialog = false },
                                content = { Text("Cancel", color = TextSecondary) }
                            )
                        },
                        containerColor = SurfaceDark,
                        titleContentColor = TextPrimary,
                        textContentColor = TextPrimary
                    )
                }

                if (!isMovie && show?.seasons != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(androidx.compose.ui.res.stringResource(com.example.R.string.seasons_and_episodes), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val seasonDetailsMap by detailsViewModel.seasonDetails.collectAsState()
                    val watchedEpisodeIds = watchlistViewModel.watchedEpisodesList.collectAsState().value[show?.id] ?: emptyList()
                    val today = java.util.Date()
                    
                    val totalEpisodes = show?.numberOfEpisodes ?: 0
                    if (totalEpisodes > 0 && isInWatchlistCheck) {
                        val progress = if (totalEpisodes > 0) watchedCount.toFloat() / totalEpisodes.toFloat() else 0f
                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Progress", fontSize = 14.sp, color = TextSecondary)
                                Row {
                                    val watchedColor = if (watchedCount == totalEpisodes) Color(0xFF66BB6A) else Color(0xFF64B5F6)
                                    Text("$watchedCount", fontSize = 14.sp, color = watchedColor, fontWeight = FontWeight.Bold)
                                    Text(" / ", fontSize = 14.sp, color = TextSecondary)
                                    Text("$totalEpisodes Episodes", fontSize = 14.sp, color = Color(0xFF66BB6A), fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF66BB6A),
                                trackColor = SurfaceDark
                            )
                        }
                    }
                    
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
                                    if (episodes.isEmpty()) {
                                        Text(androidx.compose.ui.res.stringResource(com.example.R.string.tba), color = TextSecondary, fontSize = 14.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
                                    } else {
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
                                                .padding(vertical = 8.dp)
                                                .clickable { selectedEpisode = episode },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${episode.episodeNumber}",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextTertiary,
                                                modifier = Modifier.width(32.dp)
                                            )
                                            if (episode.stillPath != null) {
                                                coil.compose.AsyncImage(
                                                    model = "https://image.tmdb.org/t/p/w227_and_h127_bestv2${episode.stillPath}",
                                                    contentDescription = null,
                                                    modifier = Modifier.size(width = 80.dp, height = 45.dp).clip(RoundedCornerShape(4.dp)),
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                            }
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
                                                    color = Color(0xFF64B5F6),
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                    modifier = Modifier.background(Color(0xFF64B5F6).copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            } else {
                                                val animatedColor by androidx.compose.animation.animateColorAsState(targetValue = if (isWatched) Color(0xFF66BB6A) else TextTertiary, label = "color")
                                                val scale by androidx.compose.animation.core.animateFloatAsState(targetValue = if (isWatched) 1.1f else 1f, label = "scale")
                                                androidx.compose.material3.IconButton(
                                                    onClick = {
                                                        if (auth.currentUser == null) {
                                                            android.widget.Toast.makeText(context, "Please sign in to track progress", android.widget.Toast.LENGTH_SHORT).show()
                                                        } else if (!isInWatchlist) {
                                                            android.widget.Toast.makeText(context, "Please subscribe to the series first", android.widget.Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            watchlistViewModel.toggleEpisodeWatched(show!!.id, episode.id)
                                                        }
                                                    },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isWatched) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                                        contentDescription = "Watched",
                                                        tint = animatedColor,
                                                        modifier = Modifier.size(28.dp).scale(scale)
                                                    )
                                                }
                                            }
                                        }
                                        HorizontalDivider(color = BorderStroke)
                                    }
                                    }
                                } else {
                                    CircularProgressIndicator(color = dynamicThemeColor, modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally))
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
                                .background(Color(0xFF64B5F6).copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(show?.nextEpisodeToAir?.episodeNumber?.toString() ?: "", color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold, fontSize = 20.sp)
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

                var userRating by remember(userReview) { mutableStateOf(userReview?.rating ?: 0f) }
                var userReviewText by remember(userReview) { mutableStateOf(userReview?.text ?: "") }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(if (userReview != null) "Edit Your Rating" else "Your Rating", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
                                focusedBorderColor = dynamicThemeColor,
                                unfocusedBorderColor = BorderStroke,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = dynamicThemeColor
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
                                    socialViewModel.publishActivity("REVIEWED", showId, show?.title ?: show?.name ?: "Unknown", "Rated $userRating/5: $userReviewText")
                                    android.widget.Toast.makeText(context, "Review saved", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = dynamicThemeColor),
                            shape = RoundedCornerShape(8.dp),
                            enabled = userRating > 0f
                        ) {
                            Text(if (userReview != null) androidx.compose.ui.res.stringResource(com.example.R.string.edit_review) else androidx.compose.ui.res.stringResource(com.example.R.string.submit_review), color = Color.White)
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
                
                var showAllReviews by remember { mutableStateOf(false) }
                val visibleReviews = if (showAllReviews) reviews else reviews.take(3)
                
                visibleReviews.forEach { review ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .background(SurfaceDark, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(32.dp).clip(androidx.compose.foundation.shape.CircleShape).background(dynamicThemeColor),
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
                
                if (reviews.size > 3 && !showAllReviews) {
                    TextButton(
                        onClick = { showAllReviews = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(androidx.compose.ui.res.stringResource(com.example.R.string.view_all_reviews), color = dynamicThemeColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
    
    selectedEpisode?.let { episode ->
        EpisodeDetailsSheet(
            episode = episode,
            onDismiss = { selectedEpisode = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeDetailsSheet(
    episode: com.example.network.TmdbEpisode,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            if (episode.stillPath != null) {
                coil.compose.AsyncImage(
                    model = "https://image.tmdb.org/t/p/w710_and_h400_multi_faces${episode.stillPath}",
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f/9f).clip(RoundedCornerShape(8.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                text = "${episode.episodeNumber}. ${episode.name ?: androidx.compose.ui.res.stringResource(com.example.R.string.tba)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (episode.voteAverage != null && episode.voteAverage > 0) {
                    Icon(androidx.compose.material.icons.Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(String.format("%.1f", episode.voteAverage), color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                }
                if (episode.runtime != null && episode.runtime > 0) {
                    Text("${episode.runtime} min", color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Text(episode.airDate ?: "", color = TextSecondary, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = episode.overview?.takeIf { it.isNotBlank() } ?: "No overview available.",
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
