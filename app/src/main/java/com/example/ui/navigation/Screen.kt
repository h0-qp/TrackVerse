package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

import com.example.R

sealed class Screen(val route: String, val titleRes: Int, val icon: ImageVector) {
    object Home : Screen("home", R.string.home, Icons.Filled.Home)
    object Discover : Screen("discover", R.string.trending, Icons.Filled.Explore)
    object Search : Screen("search", R.string.search, Icons.Filled.Search)
    object Statistics : Screen("statistics", R.string.watchlist, Icons.Filled.BarChart)
    object Social : Screen("social", R.string.social, Icons.Filled.Group)
    object Profile : Screen("profile", R.string.profile, Icons.Filled.Person)
}
