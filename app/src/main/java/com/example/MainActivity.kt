package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
  override fun attachBaseContext(newBase: android.content.Context) {
    val prefs = newBase.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
    val lang = prefs.getString("language", "en") ?: "en"
    val locale = java.util.Locale(lang)
    java.util.Locale.setDefault(locale)
    val config = android.content.res.Configuration(newBase.resources.configuration)
    config.setLocale(locale)
    val context = newBase.createConfigurationContext(config)
    super.attachBaseContext(context)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // إجبار Firebase Firestore على العمل 100% سحابياً وبدون استخدام الذاكرة المحلية (Cache)
    try {
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings
    } catch (e: Exception) {
        // قد يرمي استثناء إذا تم تهيئة Firestore في مكان آخر مسبقاً، لذلك نضع try/catch
        e.printStackTrace()
    }
    
    enableEdgeToEdge()
    
    // Request Notification Permission for Android 13+
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }
    }

    // Subscribe to topics
    com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("daily_updates")
        .addOnCompleteListener { task ->
            var msg = "Subscribed to daily_updates"
            if (!task.isSuccessful) {
                msg = "Subscribe failed"
            }
            android.util.Log.d("FCM", msg)
        }

    setContent {
      MyApplicationTheme {
        MainScreen()
      }
    }
  }
}

