package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TmdbResponse(
    @Json(name = "results") val results: List<TmdbShow>
)

@JsonClass(generateAdapter = true)
data class TmdbShow(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?,
    @Json(name = "title") val title: String?, // Movies have title
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "backdrop_path") val backdropPath: String?,
    @Json(name = "vote_average") val voteAverage: Double?,
    @Json(name = "overview") val overview: String?
)
