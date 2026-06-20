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

import com.example.R

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import com.example.ui.screens.LocalAnimatedVisibilityScope
import com.example.ui.screens.LocalSharedTransitionScope

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    
    val items = listOf(
        Screen.Home,
        Screen.Discover,
        Screen.Search,
        Screen.Social,
        Screen.Statistics,
        Screen.Profile
    )

    SharedTransitionLayout {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this
        ) {
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
                                    icon = { Icon(screen.icon, contentDescription = androidx.compose.ui.res.stringResource(screen.titleRes)) },
                                    label = { Text(androidx.compose.ui.res.stringResource(screen.titleRes), fontWeight = FontWeight.Bold) },
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
                    composable(Screen.Home.route) { 
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            HomeScreen(navController) 
                        }
                    }
                    composable(Screen.Discover.route) { 
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            DiscoverScreen(navController) 
                        }
                    }
                    composable(Screen.Search.route) { 
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            SearchScreen(navController) 
                        }
                    }
                    composable(Screen.Social.route) { 
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            SocialScreen() 
                        }
                    }
                    composable(Screen.Statistics.route) { 
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            StatisticsScreen(navController) 
                        }
                    }
                    composable(Screen.Profile.route) { 
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            ProfileScreen(navController = navController) 
                        }
                    }
                    composable("settings") { 
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            SettingsScreen(navController = navController) 
                        }
                    }
                    composable(
                        route = "details/{showId}/{isMovie}",
                        arguments = listOf(
                            androidx.navigation.navArgument("showId") { type = androidx.navigation.NavType.IntType },
                            androidx.navigation.navArgument("isMovie") { type = androidx.navigation.NavType.BoolType }
                        )
                    ) { backStackEntry ->
                        val showId = backStackEntry.arguments?.getInt("showId") ?: 0
                        val isMovie = backStackEntry.arguments?.getBoolean("isMovie") ?: false
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            DetailsScreen(
                                showId = showId,
                                isMovie = isMovie,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
