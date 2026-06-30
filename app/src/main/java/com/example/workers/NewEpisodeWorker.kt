package com.example.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.network.ApiClient
import com.example.network.TmdbShow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NewEpisodeWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val user = FirebaseAuth.getInstance().currentUser ?: return Result.success()
        val db = FirebaseFirestore.getInstance()

        try {
            val snapshot = db.collection("users").document(user.uid)
                .collection("watchlist").get().await()

            val shows = snapshot.documents.mapNotNull { doc ->
                try {
                    TmdbShow(
                        id = doc.getLong("id")?.toInt() ?: 0,
                        name = doc.getString("name"),
                        originalName = doc.getString("originalName"),
                        title = doc.getString("title")
                    )
                } catch (e: Exception) { null }
            }

            for (show in shows) {
                // Fetch latest details
                try {
                    val details = ApiClient.tmdbService.getTvDetails(show.id)
                    details.nextEpisodeToAir?.let { nextEp ->
                        // check if it airs today or recently
                        // Just as a simple notification mechanism for new episodes:
                        if (!nextEp.airDate.isNullOrEmpty()) {
                            // Let's assume we want to notify if there's a next episode
                            // In a real app we would store the last notified episode ID
                            // For simplicity, we just send one for now if we haven't seen this episode
                            val lastNotifiedEpId = applicationContext
                                .getSharedPreferences("notifications", Context.MODE_PRIVATE)
                                .getInt("last_notified_${show.id}", -1)
                            
                            if (nextEp.id != lastNotifiedEpId) {
                                sendNotification(show.displayTitle, nextEp.name ?: "New Episode", nextEp.airDate)
                                applicationContext
                                    .getSharedPreferences("notifications", Context.MODE_PRIVATE)
                                    .edit()
                                    .putInt("last_notified_${show.id}", nextEp.id)
                                    .apply()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }

    private fun sendNotification(showTitle: String, episodeName: String, airDate: String?) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "new_episodes_channel"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "New Episodes",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for new episodes of your favorite shows"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(com.example.R.drawable.ic_notification)
            .setContentTitle("New Episode: $showTitle")
            .setContentText("Episode '$episodeName' airs on $airDate")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
