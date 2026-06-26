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

@Composable
fun SocialScreen(socialViewModel: SocialViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by socialViewModel.searchResults.collectAsState()
    val following by socialViewModel.following.collectAsState()
    val feed by socialViewModel.feed.collectAsState()
    
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 2) {
            socialViewModel.searchUsers(searchQuery)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
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
            Text(stringResource(R.string.recent_activity), fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(feed) { act ->
                    val actionStr = when (act.actionType) {
                        "ADDED_WATCHLIST" -> stringResource(R.string.added_to_watchlist)
                        "REVIEWED" -> stringResource(R.string.reviewed)
                        else -> stringResource(R.string.watched_social)
                    }
                    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                    val dateStr = sdf.format(Date(act.timestamp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(SurfaceDark, RoundedCornerShape(8.dp))
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
                    }
                }
            }
        }
    }
}
