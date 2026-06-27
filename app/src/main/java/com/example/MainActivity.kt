package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

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
    setContent {
      MyApplicationTheme {
        MainScreen()
      }
    }
  }
}

