package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.NavBgDark
import com.example.ui.theme.BorderStroke

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    
    val items = listOf(
        Screen.Home,
        Screen.Discover,
        Screen.Search,
        Screen.Statistics,
        Screen.Profile
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(color = NavBgDark, shadowElevation = 0.dp) {
                HorizontalDivider(color = BorderStroke, thickness = 1.dp)
                NavigationBar(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    tonalElevation = 0.dp
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title, fontWeight = FontWeight.Bold) },
                            selected = currentRoute?.startsWith(screen.route) == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = androidx.compose.ui.graphics.Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(Screen.Discover.route) { DiscoverScreen(navController) }
            composable(Screen.Search.route) { SearchScreen(navController) }
            composable(Screen.Statistics.route) { StatisticsScreen() }
            composable(Screen.Profile.route) { ProfileScreen(navController = navController) }
            composable("settings") { SettingsScreen(navController = navController) }
            composable(
                route = "details/{showId}/{isMovie}",
                arguments = listOf(
                    androidx.navigation.navArgument("showId") { type = androidx.navigation.NavType.IntType },
                    androidx.navigation.navArgument("isMovie") { type = androidx.navigation.NavType.BoolType }
                )
            ) { backStackEntry ->
                val showId = backStackEntry.arguments?.getInt("showId") ?: 0
                val isMovie = backStackEntry.arguments?.getBoolean("isMovie") ?: false
                DetailsScreen(
                    showId = showId,
                    isMovie = isMovie,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
