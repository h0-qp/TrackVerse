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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.filled.UploadFile
import kotlin.math.min
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
                    onCheckedChange = { isEnabled -> 
                        notificationsEnabled = isEnabled 
                        val workManager = androidx.work.WorkManager.getInstance(context)
                        if (isEnabled) {
                            val request = androidx.work.PeriodicWorkRequestBuilder<com.example.notifications.NotificationWorker>(
                                24, java.util.concurrent.TimeUnit.HOURS
                            ).build()
                            workManager.enqueueUniquePeriodicWork("SmartNotifications", androidx.work.ExistingPeriodicWorkPolicy.UPDATE, request)
                            android.widget.Toast.makeText(context, "Smart Notifications Enabled", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            workManager.cancelUniqueWork("SmartNotifications")
                        }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = BlueHighlight, checkedTrackColor = BlueHighlight.copy(alpha = 0.5f))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(androidx.compose.ui.res.stringResource(com.example.R.string.data_management), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = BlueHighlight, modifier = Modifier.padding(bottom = 16.dp))

            val watchlistViewModel: com.example.viewmodel.WatchlistViewModel = viewModel()
            val coroutineScope = rememberCoroutineScope()
            var isImporting by remember { mutableStateOf(false) }

            val csvPicker = androidx.activity.compose.rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.GetContent()
            ) { uri ->
                if (uri != null) {
                    isImporting = true
                    coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val reader = java.io.BufferedReader(java.io.InputStreamReader(inputStream))
                            val lines = reader.readLines()
                            reader.close()
                            
                            // Simple CSV parsing for Title, trying to find Title column
                            if (lines.isNotEmpty()) {
                                val header = lines[0].lowercase()
                                val titleIndex = header.split(",").indexOfFirst { it.contains("title") || it.contains("name") }
                                if (titleIndex != -1) {
                                    var importedCount = 0
                                    for (i in 1 until minOf(lines.size, 10)) { // limit to 10 for safety in this demo
                                        val columns = lines[i].split(",")
                                        if (columns.size > titleIndex) {
                                            val title = columns[titleIndex].trim().removeSurrounding("\"")
                                            if (title.isNotEmpty()) {
                                                val searchResponse = com.example.network.ApiClient.tmdbService.search(title)
                                                val show = searchResponse.results.firstOrNull { 
                                                    it.title.equals(title, ignoreCase = true) || it.name.equals(title, ignoreCase = true)
                                                }
                                                if (show != null) {
                                                    watchlistViewModel.addToWatchlist(show, show.title != null)
                                                    importedCount++
                                                }
                                            }
                                        }
                                    }
                                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        android.widget.Toast.makeText(context, "Imported $importedCount shows successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        android.widget.Toast.makeText(context, "Could not find 'Title' column in CSV", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            withContext(kotlinx.coroutines.Dispatchers.Main) {
                                android.widget.Toast.makeText(context, "Failed to import: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } finally {
                            isImporting = false
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isImporting) { csvPicker.launch("text/comma-separated-values") }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(androidx.compose.ui.res.stringResource(com.example.R.string.import_csv), fontSize = 16.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                    Text(androidx.compose.ui.res.stringResource(com.example.R.string.import_csv_desc), fontSize = 12.sp, color = TextTertiary)
                }
                if (isImporting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = BlueHighlight, strokeWidth = 2.dp)
                } else {
                    Icon(androidx.compose.material.icons.Icons.Default.UploadFile, contentDescription = "Import", tint = TextPrimary)
                }
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
