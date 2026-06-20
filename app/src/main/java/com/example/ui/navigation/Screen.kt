package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Discover : Screen("discover", "Discover", Icons.Filled.Explore)
    object Search : Screen("search", "Search", Icons.Filled.Search)
    object Statistics : Screen("statistics", "Stats", Icons.Filled.BarChart)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
}
