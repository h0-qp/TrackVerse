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
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
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
        Screen.Social,
        Screen.Statistics,
        Screen.Profile
    )

    val prefs = context.getSharedPreferences("prefs", android.content.Context.MODE_PRIVATE)
    val startDest = if (prefs.getBoolean("onboarding_completed", false)) Screen.Home.route else "onboarding"

    SharedTransitionLayout {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val showBottomBar = items.any { it.route == currentRoute }

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    if (showBottomBar) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 24.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color(0xFF11141E).copy(alpha = 0.95f))
                                .border(1.dp, Color.White.copy(alpha=0.1f), RoundedCornerShape(32.dp))
                                .padding(vertical = 8.dp, horizontal = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items.forEach { screen ->
                                    val isSelected = currentRoute?.startsWith(screen.route) == true
                                    val animatedWeight by androidx.compose.animation.core.animateFloatAsState(targetValue = if (isSelected) 1.5f else 1f, label = "weight")
                                    val animatedBgColor by androidx.compose.animation.animateColorAsState(targetValue = if (isSelected) com.example.ui.theme.BlueHighlight.copy(alpha = 0.2f) else Color.Transparent, label = "bg")
                                    val animatedIconColor by androidx.compose.animation.animateColorAsState(targetValue = if (isSelected) com.example.ui.theme.BlueHighlight else Color.Gray, label = "icon")

                                    Box(
                                        modifier = Modifier
                                            .weight(animatedWeight)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(animatedBgColor)
                                            .clickable(
                                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            screen.icon,
                                            contentDescription = androidx.compose.ui.res.stringResource(screen.titleRes),
                                            tint = animatedIconColor,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = startDest,
                    modifier = Modifier.padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp),
                    enterTransition = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(300)) + slideIntoContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Start, androidx.compose.animation.core.tween(300)) },
                    exitTransition = { androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(300)) + slideOutOfContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Start, androidx.compose.animation.core.tween(300)) },
                    popEnterTransition = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(300)) + slideIntoContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.End, androidx.compose.animation.core.tween(300)) },
                    popExitTransition = { androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(300)) + slideOutOfContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.End, androidx.compose.animation.core.tween(300)) }
                ) {
                    composable("onboarding") {
                        OnboardingScreen(onComplete = {
                            prefs.edit().putBoolean("onboarding_completed", true).apply()
                            navController.navigate(Screen.Home.route) {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        })
                    }
                    composable(Screen.Home.route) { 
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) { HomeScreen(navController) }
                    }
                    composable(Screen.Discover.route) { 
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) { DiscoverScreen(navController) }
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
                    composable("ask_ai") {
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) { AiChatScreen(navController = navController) }
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

