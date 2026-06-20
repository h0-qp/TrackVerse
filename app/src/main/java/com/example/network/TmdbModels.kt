package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TmdbResponse(
    @Json(name = "results") val results: List<TmdbShow>
)

@JsonClass(generateAdapter = true)
data class TmdbEpisode(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?,
    @Json(name = "air_date") val airDate: String?,
    @Json(name = "episode_number") val episodeNumber: Int?,
    @Json(name = "season_number") val seasonNumber: Int?
)

@JsonClass(generateAdapter = true)
data class TmdbSeason(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?,
    @Json(name = "season_number") val seasonNumber: Int?,
    @Json(name = "episode_count") val episodeCount: Int?,
    @Json(name = "air_date") val airDate: String?
)

@JsonClass(generateAdapter = true)
data class TmdbSeasonResponse(
    @Json(name = "_id") val id: String? = null,
    @Json(name = "episodes") val episodes: List<TmdbEpisode>? = null
)

@JsonClass(generateAdapter = true)
data class TmdbCast(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "character") val character: String?,
    @Json(name = "profile_path") val profilePath: String?
)

@JsonClass(generateAdapter = true)
data class TmdbCredits(
    @Json(name = "cast") val cast: List<TmdbCast>?
)

@JsonClass(generateAdapter = true)
data class TmdbShow(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "title") val title: String? = null, // Movies have title
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "vote_average") val voteAverage: Double? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "number_of_episodes") val numberOfEpisodes: Int? = null,
    @Json(name = "number_of_seasons") val numberOfSeasons: Int? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "next_episode_to_air") val nextEpisodeToAir: TmdbEpisode? = null,
    @Json(name = "seasons") val seasons: List<TmdbSeason>? = null,
    @Json(name = "credits") val credits: TmdbCredits? = null
)
