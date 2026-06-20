package com.example.network

import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbService {
    @GET("3/trending/tv/day")
    suspend fun getTrendingShows(
        @Query("api_key") apiKey: String
    ): TmdbResponse

    @GET("3/trending/movie/day")
    suspend fun getTrendingMovies(
         @Query("api_key") apiKey: String
    ): TmdbResponse

    @GET("3/tv/top_rated")
    suspend fun getTopRatedShows(
         @Query("api_key") apiKey: String
    ): TmdbResponse
    
    @GET("3/search/multi")
    suspend fun search(
        @Query("api_key") apiKey: String,
        @Query("query") query: String
    ): TmdbResponse
}
