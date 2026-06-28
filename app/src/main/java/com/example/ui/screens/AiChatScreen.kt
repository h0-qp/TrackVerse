package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ui.theme.BlueHighlight
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AiChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(navController: NavController, viewModel: AiChatViewModel = viewModel()) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var input by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(androidx.compose.ui.res.stringResource(com.example.R.string.ask_ai), color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = androidx.compose.ui.res.stringResource(com.example.R.string.back), tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages) { message ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Column(
                            modifier = Modifier
                                .background(
                                    color = if (message.isUser) BlueHighlight else SurfaceDark,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp)
                                .fillMaxWidth(if (message.shows.isNotEmpty()) 0.8f else 0.7f)
                        ) {
                            Text(
                                text = message.text,
                                color = if (message.isUser) Color.White else TextPrimary,
                                fontSize = 16.sp
                            )
                            if (message.shows.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                message.shows.forEach { show ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .clickable {
                                                val isMovie = show.title != null
                                                navController.navigate("details/${show.id}/$isMovie?source=ai")
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = "https://image.tmdb.org/t/p/w92${show.posterPath}",
                                            contentDescription = show.displayTitle,
                                            modifier = Modifier
                                                .width(50.dp)
                                                .height(75.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = show.displayTitle,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = if (show.title != null) androidx.compose.ui.res.stringResource(com.example.R.string.movie_singular) else androidx.compose.ui.res.stringResource(com.example.R.string.tv_show_singular),
                                                color = TextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (isLoading) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            CircularProgressIndicator(color = BlueHighlight, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(androidx.compose.ui.res.stringResource(com.example.R.string.ask_recommendations)) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BlueHighlight,
                        unfocusedBorderColor = SurfaceDark
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        viewModel.sendMessage(input)
                        input = ""
                    },
                    modifier = Modifier
                        .background(BlueHighlight, RoundedCornerShape(24.dp))
                        .size(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = androidx.compose.ui.res.stringResource(com.example.R.string.send), tint = Color.White)
                }
            }
        }
    }
}
