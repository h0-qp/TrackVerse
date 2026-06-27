package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.R
import com.example.ui.theme.*
import com.example.viewmodel.DiscoverViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(navController: NavController, viewModel: DiscoverViewModel = viewModel()) {
    val results by viewModel.discoverResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentType by viewModel.currentType.collectAsState()
    
    var showFilters by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Discover", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            IconButton(onClick = { showFilters = true }) {
                Icon(Icons.Default.FilterList, contentDescription = "Filters", tint = TextPrimary)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BlueHighlight)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(results) { show ->
                    SearchResultItem(
                        show = show,
                        sourceKey = "discover",
                        modifier = Modifier.clickable {
                            val isMovie = currentType == "movie"
                            navController.navigate("details/${show.id}/$isMovie?source=discover")
                        }
                    )
                }
            }
        }
    }

    if (showFilters) {
        ModalBottomSheet(
            onDismissRequest = { showFilters = false },
            containerColor = SurfaceDark
        ) {
            DiscoverFilterSheet(viewModel = viewModel, onApply = {
                showFilters = false
                viewModel.applyFilters()
            })
        }
    }
}

@Composable
fun DiscoverFilterSheet(viewModel: DiscoverViewModel, onApply: () -> Unit) {
    var type by remember { mutableStateOf(viewModel.currentType.value) }
    var sortBy by remember { mutableStateOf(viewModel.selectedSortBy.value) }
    var year by remember { mutableStateOf(viewModel.selectedYear.value ?: "") }
    var rating by remember { mutableStateOf(viewModel.selectedRating.value?.toString() ?: "") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Type", color = TextPrimary, fontWeight = FontWeight.Bold)
        Row {
            RadioButton(selected = type == "tv", onClick = { type = "tv" })
            Text("TV Shows", color = TextPrimary, modifier = Modifier.align(Alignment.CenterVertically).padding(end = 16.dp))
            RadioButton(selected = type == "movie", onClick = { type = "movie" })
            Text("Movies", color = TextPrimary, modifier = Modifier.align(Alignment.CenterVertically))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Sort By", color = TextPrimary, fontWeight = FontWeight.Bold)
        Row {
            RadioButton(selected = sortBy == "popularity.desc", onClick = { sortBy = "popularity.desc" })
            Text("Popularity", color = TextPrimary, modifier = Modifier.align(Alignment.CenterVertically).padding(end = 16.dp))
            RadioButton(selected = sortBy == "vote_average.desc", onClick = { sortBy = "vote_average.desc" })
            Text("Rating", color = TextPrimary, modifier = Modifier.align(Alignment.CenterVertically))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = year,
                onValueChange = { year = it },
                label = { Text("Year") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = rating,
                onValueChange = { rating = it },
                label = { Text("Min Rating (0-10)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                viewModel.currentType.value = type
                viewModel.selectedSortBy.value = sortBy
                viewModel.selectedYear.value = year.takeIf { it.isNotBlank() }
                viewModel.selectedRating.value = rating.toFloatOrNull()
                onApply()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = BlueHighlight)
        ) {
            Text("Apply Filters", color = androidx.compose.ui.graphics.Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

