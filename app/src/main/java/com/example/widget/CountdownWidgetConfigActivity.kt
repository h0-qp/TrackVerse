package com.example.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.network.TmdbShow
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.WatchlistViewModel
import kotlinx.coroutines.launch
import androidx.glance.appwidget.updateAll
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.GlanceId

class CountdownWidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Find the widget id from the intent. 
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_CANCELED, resultValue)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: WatchlistViewModel = viewModel()
                val watchlist by viewModel.watchlist.collectAsState()
                val scope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    viewModel.loadWatchlist()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.padding(16.dp).systemBarsPadding()) {
                        Text(
                            text = "Select a Show for Countdown",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        if (watchlist.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(watchlist) { show ->
                                    ShowItem(show) {
                                        scope.launch {
                                            saveWidgetSettings(show)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun saveWidgetSettings(show: TmdbShow) {
        val showName = show.name ?: show.title ?: "Unknown Show"
        val nextEp = show.nextEpisodeToAir
        val airDate = nextEp?.airDate ?: ""
        val posterPath = show.posterPath ?: ""
        val backdropPath = show.backdropPath ?: ""

        // Save to SharedPreferences for normal access if needed, or Glance state
        val prefs = getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("widget_${appWidgetId}_showId", show.id)
            .putString("widget_${appWidgetId}_showName", showName)
            .putString("widget_${appWidgetId}_airDate", airDate)
            .putString("widget_${appWidgetId}_posterPath", posterPath)
            .putString("widget_${appWidgetId}_backdropPath", backdropPath)
            .apply()

        // Also update the widget
        val manager = GlanceAppWidgetManager(this)
        val glanceId = manager.getGlanceIdBy(appWidgetId)
        CountdownWidget().update(this, glanceId)

        // Return OK
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}

@Composable
fun ShowItem(show: TmdbShow, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w200${show.posterPath}",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = show.name ?: show.title ?: "Unknown",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (show.nextEpisodeToAir != null) {
                Text(
                    text = "Next ep: ${show.nextEpisodeToAir.airDate}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "No upcoming episode",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
