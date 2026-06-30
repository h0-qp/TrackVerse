package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.appwidget.cornerRadius
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class CountdownWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val manager = GlanceAppWidgetManager(context)
        val appWidgetId = manager.getAppWidgetId(id)

        val prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
        val showName = prefs.getString("widget_${appWidgetId}_showName", "No Show Selected") ?: "No Show Selected"
        val airDate = prefs.getString("widget_${appWidgetId}_airDate", "") ?: ""

        var countdownText = "Unknown"
        if (airDate.isNotEmpty()) {
            try {
                val date = LocalDate.parse(airDate, DateTimeFormatter.ISO_LOCAL_DATE)
                val today = LocalDate.now()
                val days = ChronoUnit.DAYS.between(today, date)
                countdownText = if (days < 0) {
                    "Aired"
                } else if (days == 0L) {
                    "Today"
                } else {
                    "$days Days"
                }
            } catch (e: Exception) {
                countdownText = "N/A"
            }
        } else if (showName != "No Show Selected") {
            countdownText = "TBA"
        }

        provideContent {
            WidgetContent(showName, countdownText)
        }
    }

    @Composable
    private fun WidgetContent(showName: String, countdownText: String) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(16.dp)
                .background(Color(0xFF141414)) // A deep nice dark color
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Next Episode",
                style = TextStyle(color = ColorProvider(Color(0xFFAAAAAA)), fontWeight = FontWeight.Medium, fontSize = 14.sp)
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = countdownText,
                style = TextStyle(color = ColorProvider(Color(0xFFE50914)), fontWeight = FontWeight.Bold, fontSize = 28.sp)
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = showName,
                style = TextStyle(color = ColorProvider(Color.White), fontWeight = FontWeight.Bold, fontSize = 16.sp),
                maxLines = 1
            )
        }
    }
}

class CountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownWidget()
}

