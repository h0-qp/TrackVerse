package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.theme.*

import android.app.Activity
import java.util.Locale
import androidx.compose.ui.platform.LocalContext

import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
    val currentLangCode = prefs.getString("language", "en") ?: "en"
    var selectedLanguage by remember { mutableStateOf(if (currentLangCode == "ar") "العربية" else "English") }
    var notificationsEnabled by remember { mutableStateOf(true) }

    fun changeLanguage(language: String) {
        selectedLanguage = language
        val localeCode = if (language == "العربية") "ar" else "en"
        prefs.edit().putString("language", localeCode).apply()
        
        val locale = Locale(localeCode)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        if (context is Activity) {
            val intent = context.intent
            context.finish()
            context.startActivity(intent)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(androidx.compose.ui.res.stringResource(R.string.settings), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text(androidx.compose.ui.res.stringResource(R.string.preferences), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = BlueHighlight, modifier = Modifier.padding(bottom = 16.dp))
            
            // Language Selection
            Text(androidx.compose.ui.res.stringResource(R.string.language), fontSize = 16.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LanguageOption("English", selectedLanguage == "English") { changeLanguage("English") }
                LanguageOption("العربية", selectedLanguage == "العربية") { changeLanguage("العربية") }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Notifications
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(androidx.compose.ui.res.stringResource(R.string.notifications), fontSize = 16.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                    Text(androidx.compose.ui.res.stringResource(R.string.remind_episodes), fontSize = 12.sp, color = TextTertiary)
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = BlueHighlight, checkedTrackColor = BlueHighlight.copy(alpha = 0.5f))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(androidx.compose.ui.res.stringResource(R.string.about), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = BlueHighlight, modifier = Modifier.padding(bottom = 16.dp))
            Text(androidx.compose.ui.res.stringResource(R.string.app_name) + " v1.0.0", fontSize = 16.sp, color = TextPrimary)
        }
    }
}

@Composable
fun LanguageOption(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .background(if (isSelected) BlueHighlight else SurfaceDark, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) BgDark else TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}
