package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Permission accepted
            } else {
                // Permission denied
            }
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            
            if (!isGranted) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF141A29).copy(alpha = 0.85f))
                            .border(1.dp, Color.White.copy(alpha=0.1f), RoundedCornerShape(24.dp))
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary,
                            tonalElevation = 0.dp,
                            windowInsets = WindowInsets(0,0,0,0) // To remove default navbar padding in floating mode
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
                                        indicatorColor = Color.Transparent
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
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding() / 2) // Less padding since it's floating
                ) {
                    composable(Screen.Home.route) { 
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) { HomeScreen(navController) }
                    }
                    composable(Screen.Discover.route) { 
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) { DiscoverScreen(navController) }
                    }
                    composable(Screen.Search.route) { 
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) { SearchScreen(navController) }
                    }
                    composable(Screen.Social.route) { 
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) { SocialScreen() }
                    }
                    composable(Screen.Statistics.route) { 
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) { StatisticsScreen(navController) }
                    }
                    composable(Screen.Profile.route) { 
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) { ProfileScreen(navController = navController) }
                    }
                    composable("settings") { 
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) { SettingsScreen(navController = navController) }
                    }
                    composable(
                        route = "details/{showId}/{isMovie}?source={source}",
                        arguments = listOf(
                            androidx.navigation.navArgument("showId") { type = androidx.navigation.NavType.IntType },
                            androidx.navigation.navArgument("isMovie") { type = androidx.navigation.NavType.BoolType },
                            androidx.navigation.navArgument("source") { 
                                type = androidx.navigation.NavType.StringType
                                nullable = true
                                defaultValue = ""
                            }
                        )
                    ) { backStackEntry ->
                        val showId = backStackEntry.arguments?.getInt("showId") ?: 0
                        val isMovie = backStackEntry.arguments?.getBoolean("isMovie") ?: false
                        val source = backStackEntry.arguments?.getString("source") ?: ""
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            DetailsScreen(
                                showId = showId,
                                isMovie = isMovie,
                                sourceKey = source,
                                onBack = { navController.popBackStack() },
                                onPersonClick = { personId ->
                                    navController.navigate("actor/$personId")
                                }
                            )
                        }
                    }
                    composable(
                        route = "actor/{personId}",
                        arguments = listOf(
                            androidx.navigation.navArgument("personId") { type = androidx.navigation.NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val personId = backStackEntry.arguments?.getInt("personId") ?: 0
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            PersonScreen(
                                personId = personId,
                                onBack = { navController.popBackStack() },
                                onShowClick = { showId, isMovie ->
                                    navController.navigate("details/$showId/$isMovie")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

