package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.BlueHighlight
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val illustration: @Composable () -> Unit
)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pages = listOf(
        OnboardingPage(
            title = androidx.compose.ui.res.stringResource(com.example.R.string.onboarding_title_1),
            description = androidx.compose.ui.res.stringResource(com.example.R.string.onboarding_desc_1),
            illustration = { WelcomeIllustration() }
        ),
        OnboardingPage(
            title = androidx.compose.ui.res.stringResource(com.example.R.string.onboarding_title_2),
            description = androidx.compose.ui.res.stringResource(com.example.R.string.onboarding_desc_2),
            illustration = { ProgressIllustration() }
        ),
        OnboardingPage(
            title = androidx.compose.ui.res.stringResource(com.example.R.string.onboarding_title_3),
            description = androidx.compose.ui.res.stringResource(com.example.R.string.onboarding_desc_3),
            illustration = { CommunityIllustration() }
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF11141E), Color(0xFF0B0D14))
                )
            )
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { position ->
            OnboardingPageContent(pages[position])
        }

        // Indicators
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) BlueHighlight else Color.DarkGray
                val width by animateDpAsState(
                    targetValue = if (pagerState.currentPage == iteration) 28.dp else 10.dp,
                    label = "indicator"
                )
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .height(10.dp)
                        .width(width)
                )
            }
        }

        // Next / Start Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(32.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onComplete()
                    }
                },
                containerColor = BlueHighlight,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(64.dp)
            ) {
                if (pagerState.currentPage < pages.size - 1) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = androidx.compose.ui.res.stringResource(com.example.R.string.next), modifier = Modifier.size(28.dp))
                } else {
                    Icon(Icons.Default.Check, contentDescription = androidx.compose.ui.res.stringResource(com.example.R.string.start), modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        page.illustration()
        
        Spacer(modifier = Modifier.height(64.dp))
        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = page.description,
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun WelcomeIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "welcome")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "welcome_float"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        // Back left poster
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w300/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg", // Deadpool
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .offset(x = (-70).dp, y = (10 + (floatAnim * 10)).dp)
                .graphicsLayer { rotationZ = -15f; scaleX = 0.85f; scaleY = 0.85f; alpha = 0.6f }
                .size(130.dp, 190.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        // Back right poster
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w300/rSPw7tgCH9c6NqICZef4kZjFOQ5.jpg", // Godfather
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .offset(x = 70.dp, y = (20 - (floatAnim * 10)).dp)
                .graphicsLayer { rotationZ = 15f; scaleX = 0.85f; scaleY = 0.85f; alpha = 0.6f }
                .size(130.dp, 190.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        // Center main poster
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w300/qJ2tW6WMUDux911r6m7haRef0WH.jpg", // Dark Knight
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .offset(y = (-10 + (floatAnim * 15)).dp)
                .graphicsLayer { rotationZ = (floatAnim * 4 - 2f) }
                .size(160.dp, 240.dp)
                .shadow(24.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
        )
    }
}

@Composable
fun ProgressIllustration() {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val progressAnim by animateFloatAsState(
        targetValue = if (isVisible) 0.75f else 0f,
        animationSpec = tween(2000, delayMillis = 300, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        // Ambient glow
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(BlueHighlight.copy(alpha = 0.3f), Color.Transparent)
                    )
                )
        )
        // Abstract card
        Box(
            modifier = Modifier
                .size(260.dp, 160.dp)
                .graphicsLayer { rotationX = 15f; rotationY = -15f; shadowElevation = 30f }
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF2A2D3E), Color(0xFF1E1F29))))
                .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF383C50))) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.align(Alignment.Center), tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Box(modifier = Modifier
                            .size(100.dp, 12.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(0.8f)))
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier
                            .size(60.dp, 10.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(0.4f)))
                    }
                }
                
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("S02 E04", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("${(progressAnim * 100).toInt()}%", color = BlueHighlight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(0.3f))) {
                        Box(modifier = Modifier
                            .fillMaxWidth(progressAnim)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFF4FACFE), Color(0xFF00F2FE)))
                            ))
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "community")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "community_float"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        // Center icon
        Box(
            modifier = Modifier
                .size(90.dp)
                .graphicsLayer { scaleX = 1f + (floatAnim * 0.05f); scaleY = 1f + (floatAnim * 0.05f) }
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF00F2FE), BlueHighlight)))
                .shadow(20.dp, CircleShape)
                .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
        }
        
        // Avatar 1
        Box(modifier = Modifier
            .offset(x = (-90).dp, y = (-70 + (floatAnim * 10)).dp)
            .size(60.dp)
            .clip(CircleShape)
            .background(Color(0xFFE91E63))
            .border(2.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
             Text("M", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
        // Avatar 2
        Box(modifier = Modifier
            .offset(x = 80.dp, y = (-60 - (floatAnim * 10)).dp)
            .size(70.dp)
            .clip(CircleShape)
            .background(Color(0xFF9C27B0))
            .border(2.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
             Text("A", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
        }
        // Avatar 3
        Box(modifier = Modifier
            .offset(x = (-30).dp, y = (100 + (floatAnim * 5)).dp)
            .size(50.dp)
            .clip(CircleShape)
            .background(Color(0xFF4CAF50))
            .border(2.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
             Text("K", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        
        // Add decorative dots or small glowing stars
        Box(modifier = Modifier
            .offset(x = 60.dp, y = 80.dp)
            .size(10.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.5f)))
        Box(modifier = Modifier
            .offset(x = (-70).dp, y = 40.dp)
            .size(14.dp)
            .clip(CircleShape)
            .background(BlueHighlight.copy(alpha = 0.6f)))
    }
}

