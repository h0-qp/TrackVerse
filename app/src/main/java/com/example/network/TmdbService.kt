package com.example.network

import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbService {
    @GET("3/trending/tv/day")
    suspend fun getTrendingShows(): TmdbResponse

    @GET("3/trending/movie/day")
    suspend fun getTrendingMovies(): TmdbResponse

    @GET("3/tv/top_rated")
    suspend fun getTopRatedShows(): TmdbResponse
    
    @GET("3/tv/popular")
    suspend fun getPopularShows(): TmdbResponse
    
    @GET("3/search/multi")
    suspend fun search(
        @Query("query") query: String
    ): TmdbResponse

    @GET("3/tv/{tv_id}")
    suspend fun getTvDetails(
        @retrofit2.http.Path("tv_id") id: Int,
        @Query("append_to_response") appendToResponse: String = "credits,videos"
    ): TmdbShow

    @GET("3/tv/{tv_id}/season/{season_number}")
    suspend fun getSeasonDetails(
        @retrofit2.http.Path("tv_id") tvId: Int,
        @retrofit2.http.Path("season_number") seasonNumber: Int
    ): TmdbSeasonResponse

    @GET("3/movie/{movie_id}")
    suspend fun getMovieDetails(
        @retrofit2.http.Path("movie_id") id: Int,
        @Query("append_to_response") appendToResponse: String = "credits,videos"
    ): TmdbShow

    @GET("3/person/{person_id}")
    suspend fun getPersonDetails(
        @retrofit2.http.Path("person_id") id: Int
    ): TmdbPerson

    @GET("3/person/{person_id}/combined_credits")
    suspend fun getPersonCredits(
        @retrofit2.http.Path("person_id") id: Int
    ): TmdbPersonCredits
}
