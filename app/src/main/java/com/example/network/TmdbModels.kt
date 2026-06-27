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
    @Json(name = "season_number") val seasonNumber: Int?,
    @Json(name = "still_path") val stillPath: String?,
    @Json(name = "overview") val overview: String?,
    @Json(name = "vote_average") val voteAverage: Double?,
    @Json(name = "runtime") val runtime: Int?
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
data class TmdbVideo(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "key") val key: String,
    @Json(name = "site") val site: String,
    @Json(name = "type") val type: String
)

@JsonClass(generateAdapter = true)
data class TmdbVideosResponse(
    @Json(name = "results") val results: List<TmdbVideo>?
)

@JsonClass(generateAdapter = true)
data class TmdbCredits(
    @Json(name = "cast") val cast: List<TmdbCast>?
)

@JsonClass(generateAdapter = true)
data class TmdbShow(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "original_name") val originalName: String? = null,
    @Json(name = "title") val title: String? = null, // Movies have title
    @Json(name = "original_title") val originalTitle: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "vote_average") val voteAverage: Double? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "number_of_episodes") val numberOfEpisodes: Int? = null,
    @Json(name = "number_of_seasons") val numberOfSeasons: Int? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "next_episode_to_air") val nextEpisodeToAir: TmdbEpisode? = null,
    @Json(name = "seasons") val seasons: List<TmdbSeason>? = null,
    @Json(name = "credits") val credits: TmdbCredits? = null,
    @Json(name = "videos") val videos: TmdbVideosResponse? = null
) {
    val displayTitle: String
        get() = originalName ?: originalTitle ?: name ?: title ?: "Unknown"
}

@JsonClass(generateAdapter = true)
data class TmdbPerson(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "profile_path") val profilePath: String? = null,
    @Json(name = "biography") val biography: String? = null,
    @Json(name = "birthday") val birthday: String? = null,
    @Json(name = "deathday") val deathday: String? = null,
    @Json(name = "place_of_birth") val placeOfBirth: String? = null,
    @Json(name = "known_for_department") val knownForDepartment: String? = null
)

@JsonClass(generateAdapter = true)
data class TmdbPersonCredits(
    @Json(name = "id") val id: Int,
    @Json(name = "cast") val cast: List<TmdbShow>? = null,
    @Json(name = "crew") val crew: List<TmdbShow>? = null
)
