package com.rzrasel.youtubevideodownload.youtubevideodownload.data.api

import com.rzrasel.youtubevideodownload.core.Constant
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiClient @Inject constructor() {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(Constant.CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(Constant.READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(Constant.WRITE_TIMEOUT, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constant.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val youTubeApiService: YouTubeApiService = retrofit.create(YouTubeApiService::class.java)
}