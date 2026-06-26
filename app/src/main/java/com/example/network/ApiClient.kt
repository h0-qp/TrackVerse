package com.example.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.example.BuildConfig

object ApiClient {
    private const val BASE_URL = "https://api.themoviedb.org/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val currentLang = java.util.Locale.getDefault().language
        val langParam = if (currentLang == "ar") "ar-SA" else "en-US"
        val imageLangParam = if (currentLang == "ar") "ar,en,null" else "en,null"
        
        val url = originalRequest.url.newBuilder()
            .addQueryParameter("language", langParam)
            .addQueryParameter("include_image_language", imageLangParam)
            .build()
            
        val request = originalRequest.newBuilder()
            .url(url)
            .addHeader("Authorization", "Bearer ${BuildConfig.TMDB_API_KEY}")
            .addHeader("accept", "application/json")
            .build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    val tmdbService: TmdbService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TmdbService::class.java)
    }
}
