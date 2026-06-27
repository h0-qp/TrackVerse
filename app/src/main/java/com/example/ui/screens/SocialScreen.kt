package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.theme.*
import com.example.viewmodel.SocialViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.viewmodel.CustomListViewModel
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.clickable
import androidx.compose.ui.zIndex
import androidx.compose.foundation.border
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun SocialScreen(
    socialViewModel: SocialViewModel = viewModel(),
    customListViewModel: CustomListViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by socialViewModel.searchResults.collectAsState()
    val following by socialViewModel.following.collectAsState()
    val feed by socialViewModel.feed.collectAsState()
    val publicLists by customListViewModel.publicLists.collectAsState()
    val isLoading by socialViewModel.isLoading.collectAsState()
    
    var isSearching by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 2) {
            socialViewModel.searchUsers(searchQuery)
        }
    }

    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = { socialViewModel.loadFeed() },
        modifier = Modifier.fillMaxSize().background(BgDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
        Text(stringResource(R.string.social), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                if (it.isEmpty()) isSearching = false else isSearching = true
            },
            placeholder = { Text(stringResource(R.string.find_users), color = TextTertiary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextTertiary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BlueHighlight,
                unfocusedBorderColor = BorderStroke,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
            ),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        if (isSearching) {
            if (searchResults.isEmpty() && searchQuery.length > 2) {
                Text(stringResource(R.string.no_users_found), color = TextTertiary, modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(searchResults) { user ->
                        val isFollowing = following.contains(user.uid)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .background(SurfaceDark, RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(BlueHighlight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(user.displayName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.displayName, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                Text(user.email, color = TextTertiary, fontSize = 12.sp)
                            }
                            Button(
                                onClick = { socialViewModel.toggleFollow(user.uid) },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isFollowing) Color.DarkGray else BlueHighlight)
                            ) {
                                Text(if (isFollowing) stringResource(R.string.following) else stringResource(R.string.follow), color = Color.White)
                            }
                        }
                    }
                }
            }
        } else {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = BlueHighlight,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = BlueHighlight
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.activity_feed), color = if (selectedTab == 0) BlueHighlight else TextSecondary) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.public_lists), color = if (selectedTab == 1) BlueHighlight else TextSecondary) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(feed) { act ->
                        val actionStr = when (act.actionType) {
                            "ADDED_WATCHLIST" -> stringResource(R.string.added_to_watchlist)
                            "REVIEWED" -> stringResource(R.string.reviewed)
                            "SUGGESTED" -> stringResource(R.string.suggested)
                            else -> stringResource(R.string.watched_social)
                        }
                        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                        val dateStr = sdf.format(Date(act.timestamp))
                        
                        val isSuggestionForMe = act.actionType == "SUGGESTED" && act.targetUserId == socialViewModel.currentUserId

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .background(if (isSuggestionForMe) Color(0xFF2C3E50) else SurfaceDark, RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSuggestionForMe) 2.dp else 0.dp,
                                    color = if (isSuggestionForMe) BlueHighlight else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(16.dp)
                        ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFF555555)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(act.userName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(act.userName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(dateStr, color = TextTertiary, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Text("$actionStr ", color = TextSecondary, fontSize = 14.sp)
                            Text(act.showName, color = BlueHighlight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        if (act.details.isNotBlank()) {
                            Text(act.details, color = TextTertiary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Like and Comment Actions
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val userUid = socialViewModel.currentUserId
                            val isLiked = userUid != null && act.likes.contains(userUid)
                            
                            Icon(
                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = stringResource(R.string.like),
                                tint = if (isLiked) Color.Red else TextSecondary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { socialViewModel.toggleLike(act.id) }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${act.likes.size}", color = TextSecondary, fontSize = 12.sp)
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            var showCommentInput by remember { mutableStateOf(false) }
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = stringResource(R.string.comment),
                                tint = TextSecondary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { showCommentInput = !showCommentInput }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${act.comments.size}", color = TextSecondary, fontSize = 12.sp)
                        }
                        
                        // Comments Section
                        if (act.comments.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF1E2638), RoundedCornerShape(8.dp)).padding(8.dp)) {
                                act.comments.forEach { comment ->
                                    Row(modifier = Modifier.padding(bottom = 4.dp)) {
                                        Text("${comment.userName}: ", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
                                        Text(comment.text, color = TextSecondary, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                        
                        // Comment Input
                        var commentText by remember { mutableStateOf("") }
                        if (commentText.isNotEmpty() || act.comments.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = commentText,
                                    onValueChange = { commentText = it },
                                    placeholder = { Text(stringResource(R.string.add_a_comment), fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent, unfocusedBorderColor = Color.DarkGray)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = {
                                    if (commentText.isNotBlank()) {
                                        socialViewModel.addComment(act.id, commentText)
                                        commentText = ""
                                    }
                                }) {
                                    Text(stringResource(R.string.post), color = BlueHighlight, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(publicLists) { list ->
                        Card(
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp).fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.List, contentDescription = null, tint = BlueHighlight)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(list.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
                                    }
                                    Text(stringResource(R.string.by, list.ownerName), color = TextSecondary, fontSize = 12.sp)
                                }
                                
                                Text(stringResource(R.string.items, list.shows.size), color = TextSecondary, fontSize = 14.sp)
                                
                                Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                                    list.shows.take(5).forEachIndexed { index, show ->
                                        if (show.posterPath != null) {
                                            AsyncImage(
                                                model = "https://image.tmdb.org/t/p/w200${show.posterPath}",
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(36.dp, 54.dp)
                                                    .zIndex((5 - index).toFloat())
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .border(1.dp, SurfaceDark, RoundedCornerShape(4.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp, 54.dp)
                                                    .zIndex((5 - index).toFloat())
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color.DarkGray)
                                                    .border(1.dp, SurfaceDark, RoundedCornerShape(4.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(if (show.isMovie) Icons.Default.Movie else Icons.Default.Tv, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                    if (list.shows.size > 5) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp, 54.dp)
                                                .zIndex(0f)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF333333))
                                                .border(1.dp, SurfaceDark, RoundedCornerShape(4.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("+${list.shows.size - 5}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }
}
