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
    
    @GET("3/movie/top_rated")
    suspend fun getTopRatedMovies(): TmdbResponse
    
    @GET("3/tv/popular")
    suspend fun getPopularShows(): TmdbResponse
    
    @GET("3/discover/tv")
    suspend fun discoverTv(
        @Query("with_genres") withGenres: String? = null,
        @Query("first_air_date_year") firstAirDateYear: String? = null,
        @Query("vote_average.gte") voteAverageGte: Float? = null,
        @Query("sort_by") sortBy: String? = null
    ): TmdbResponse

    @GET("3/discover/movie")
    suspend fun discoverMovie(
        @Query("with_genres") withGenres: String? = null,
        @Query("primary_release_year") primaryReleaseYear: String? = null,
        @Query("vote_average.gte") voteAverageGte: Float? = null,
        @Query("sort_by") sortBy: String? = null
    ): TmdbResponse

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

    @GET("3/movie/{movie_id}/similar")
    suspend fun getSimilarMovies(
        @retrofit2.http.Path("movie_id") id: Int
    ): TmdbResponse

    @GET("3/tv/{tv_id}/similar")
    suspend fun getSimilarShows(
        @retrofit2.http.Path("tv_id") id: Int
    ): TmdbResponse

    @GET("3/person/{person_id}")
    suspend fun getPersonDetails(
        @retrofit2.http.Path("person_id") id: Int
    ): TmdbPerson

    @GET("3/person/{person_id}/combined_credits")
    suspend fun getPersonCredits(
        @retrofit2.http.Path("person_id") id: Int
    ): TmdbPersonCredits

    @GET("3/movie/{movie_id}/watch/providers")
    suspend fun getMovieWatchProviders(
        @retrofit2.http.Path("movie_id") id: Int
    ): WatchProvidersResponse

    @GET("3/tv/{tv_id}/watch/providers")
    suspend fun getTvWatchProviders(
        @retrofit2.http.Path("tv_id") id: Int
    ): WatchProvidersResponse
}
