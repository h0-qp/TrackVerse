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
        @retrofit2.http.Path("tv_id") id: Int
    ): TmdbShow

    @GET("3/movie/{movie_id}")
    suspend fun getMovieDetails(
        @retrofit2.http.Path("movie_id") id: Int
    ): TmdbShow
}
