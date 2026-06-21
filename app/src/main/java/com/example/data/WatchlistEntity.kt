package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.network.TmdbShow

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val id: Int,
    val name: String?,
    val originalName: String?,
    val title: String?,
    val originalTitle: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val voteAverage: Double?,
    val overview: String?,
    val isMovie: Boolean
) {
    fun toTmdbShow() = TmdbShow(
        id = id,
        name = name,
        originalName = originalName,
        title = title,
        originalTitle = originalTitle,
        posterPath = posterPath,
        backdropPath = backdropPath,
        voteAverage = voteAverage,
        overview = overview
    )

    companion object {
        fun fromTmdbShow(show: TmdbShow, isMovie: Boolean) = WatchlistEntity(
            id = show.id,
            name = show.name,
            originalName = show.originalName,
            title = show.title,
            originalTitle = show.originalTitle,
            posterPath = show.posterPath,
            backdropPath = show.backdropPath,
            voteAverage = show.voteAverage,
            overview = show.overview,
            isMovie = isMovie
        )
    }
}
