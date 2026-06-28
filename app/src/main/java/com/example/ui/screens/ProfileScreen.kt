package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import com.example.ui.theme.*
import com.example.viewmodel.AuthViewModel

import androidx.compose.ui.res.stringResource
import com.example.R

import com.example.viewmodel.WatchlistViewModel
import com.example.viewmodel.SocialViewModel
import com.example.viewmodel.CustomListViewModel
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.BarChart

@Composable
fun ProfileScreen(
    navController: NavController? = null,
    viewModel: AuthViewModel = viewModel(),
    watchlistViewModel: WatchlistViewModel = viewModel(),
    socialViewModel: SocialViewModel = viewModel(),
    customListViewModel: CustomListViewModel = viewModel()
) {
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val watchlist by watchlistViewModel.watchlist.collectAsState()
    val watchedList by watchlistViewModel.watchedEpisodesList.collectAsState()
    val following by socialViewModel.following.collectAsState()
    val myCustomLists by customListViewModel.myLists.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    
    var showCreateListDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }
    var newListPublic by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleGoogleSignInResult(result.data)
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(stringResource(R.string.edit_profile), color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text(stringResource(R.string.username)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.updateUsername(editName)
                    showEditDialog = false 
                }) {
                    Text(stringResource(R.string.save), color = BlueHighlight)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(stringResource(R.string.cancel), color = TextSecondary)
                }
            },
            containerColor = SurfaceDark,
            textContentColor = TextSecondary
        )
    }

    if (showCreateListDialog) {
        AlertDialog(
            onDismissRequest = { showCreateListDialog = false },
            title = { Text("Create Custom List", color = TextPrimary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newListName,
                        onValueChange = { newListName = it },
                        label = { Text("List Name") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = newListPublic,
                            onCheckedChange = { newListPublic = it }
                        )
                        Text("Make public (shareable)", color = TextPrimary)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    if (newListName.isNotBlank()) {
                        customListViewModel.createList(newListName, newListPublic)
                        showCreateListDialog = false
                        newListName = ""
                        newListPublic = false
                    }
                }) {
                    Text("Create", color = BlueHighlight)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateListDialog = false }) {
                    Text(stringResource(R.string.cancel), color = TextSecondary)
                }
            },
            containerColor = SurfaceDark,
            textContentColor = TextSecondary
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = BlueHighlight)
                return@Column
            }

            if (user != null) {
                // Logged in UI
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark)
                        .border(2.dp, BorderStroke, CircleShape)
                ) {
                    AsyncImage(
                        model = user?.photoUrl ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=${user?.displayName}",
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user?.displayName ?: stringResource(R.string.guest_user),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { 
                        editName = user?.displayName ?: ""
                        showEditDialog = true 
                    }) {
                        Icon(androidx.compose.material.icons.Icons.Default.Settings, contentDescription = "Edit Profile", tint = BlueHighlight)
                    }
                }
                Text(
                    text = user?.email ?: stringResource(R.string.anonymous),
                    fontSize = 14.sp,
                    color = TextTertiary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Stats Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val totalEpisodes = watchedList.values.sumOf { it.size }
                    ProfileStatItem(title = stringResource(R.string.following), value = following.size.toString())
                    ProfileStatItem(title = stringResource(R.string.watchlist_title), value = watchlist.size.toString())
                    ProfileStatItem(title = stringResource(R.string.episodes_watched), value = totalEpisodes.toString())
                }

                // Productivity Summary
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = stringResource(R.string.productivity_summary),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Last Watched Card
                    val lastWatchedShow = watchlist.lastOrNull() // Simplify: just pick the last added or modified
                    val lastWatchedCount = if (lastWatchedShow != null) watchedList[lastWatchedShow.id]?.size ?: 0 else 0
                    val totalEpsForLastWatched = lastWatchedShow?.numberOfEpisodes ?: 1
                    
                    val episodesLeft = watchlist.filter { it.title == null }.sumOf { (it.numberOfEpisodes ?: 0) - (watchedList[it.id]?.size ?: 0) }.coerceAtLeast(0)
                    val moviesLeft = watchlist.count { it.title != null }

                    Card(
                        modifier = Modifier.weight(1f).height(180.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.last_watched_record), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = "https://image.tmdb.org/t/p/w200${lastWatchedShow?.posterPath}",
                                    contentDescription = null,
                                    modifier = Modifier.size(50.dp, 75.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(stringResource(R.string.last_watched_prefix), fontSize = 10.sp, color = TextTertiary)
                                    Text(lastWatchedShow?.displayTitle ?: "None", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { if (totalEpsForLastWatched > 0) lastWatchedCount.toFloat() / totalEpsForLastWatched else 0f },
                                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                        color = BlueHighlight,
                                        trackColor = Color.DarkGray
                                    )
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Icon(Icons.Default.Tv, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                                    Text("${stringResource(R.string.episodes_left)}: $episodesLeft", fontSize = 10.sp, color = TextSecondary)
                                }
                                Column {
                                    Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                                    Text("${stringResource(R.string.movies_left)}: $moviesLeft", fontSize = 10.sp, color = TextSecondary)
                                }
                            }
                        }
                    }

                    // Recommended Playlist Card
                    val recommendedShows = watchlist.shuffled().take(3)
                    Card(
                        modifier = Modifier.weight(1f).height(180.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.recommended_playlist), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 1)
                            Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                                recommendedShows.forEachIndexed { index, show ->
                                    AsyncImage(
                                        model = "https://image.tmdb.org/t/p/w200${show.posterPath}",
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(40.dp, 60.dp)
                                            .zIndex((3 - index).toFloat())
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, SurfaceDark, RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            Text(stringResource(R.string.high_rated_similar), fontSize = 10.sp, color = TextTertiary, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 2)
                            Button(
                                onClick = { /* Navigate to a playlist screen or play first item */ },
                                modifier = Modifier.fillMaxWidth().height(32.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(stringResource(R.string.start_playlist), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Custom Lists Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Custom Lists",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = { showCreateListDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Create List", tint = BlueHighlight)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (myCustomLists.isEmpty()) {
                    Text("You haven't created any custom lists yet.", color = TextSecondary, fontSize = 14.sp)
                } else {
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(myCustomLists.size) { index ->
                            val list = myCustomLists[index]
                            Card(
                                modifier = Modifier.width(160.dp).height(100.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp).fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.List, contentDescription = null, tint = BlueHighlight)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(list.name, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1)
                                    Text("${list.shows.size} items", fontSize = 12.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = { 
                        viewModel.signOut()
                        watchlistViewModel.clearWatchlist()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = TextPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(stringResource(R.string.sign_out), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            } else {
                // Logged out UI
                Text(stringResource(R.string.app_name), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = BlueHighlight, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.sign_in_to_sync), fontSize = 16.sp, color = TextTertiary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; viewModel.clearError() },
                    label = { Text(stringResource(R.string.email)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; viewModel.clearError() },
                    label = { Text(stringResource(R.string.password)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.signInWithEmail(email, password) },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = TextPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(stringResource(R.string.login_register_email), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(stringResource(R.string.or), fontSize = 14.sp, color = TextTertiary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { launcher.launch(viewModel.getGoogleSignInIntent()) },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueHighlight, contentColor = TextPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(stringResource(R.string.sign_in_google), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = { viewModel.signInAsGuest() }) {
                    Text(stringResource(R.string.continue_guest), color = TextSecondary, fontSize = 14.sp)
                }

                if (error != null) {
                    Text(text = error!!, color = ErrorColor, fontSize = 12.sp, modifier = Modifier.padding(top = 16.dp))
                }
            }
        }
        
        // Settings Gear Icon
        if (user != null) {
            IconButton(
                onClick = { navController?.navigate("settings") },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = TextPrimary
                )
            }
        }
    }
}

@Composable
fun ProfileStatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BlueHighlight)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, fontSize = 14.sp, color = TextTertiary)
    }
}