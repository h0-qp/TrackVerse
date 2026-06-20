package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistItem(
    @PrimaryKey val id: Int, // The TMDb ID
    val title: String,
    val posterPath: String,
    val status: String // "Watching", "Completed", "Plan To Watch", etc.
)
