package com.rzrasel.instagramvideodownload.instagramdownloader.di

import com.rzrasel.instagramvideodownload.core.Constants
import com.rzrasel.instagramvideodownload.instagramdownloader.data.api.InstagramApiService
import com.rzrasel.instagramvideodownload.instagramdownloader.data.datasource.InstagramRemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlin.random.Random

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val userAgents = listOf(
        "Mozilla/5.0 (Linux; Android 10; SM-G975F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Mobile/15E148 Safari/604.1",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
    )

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor { chain ->
            var attempt = 0
            var response: okhttp3.Response? = null
            while (attempt < 3) {
                try {
                    val request = chain.request().newBuilder()
                        .header("Accept", if (chain.request().url.toString().endsWith("extract-video-url")) "application/json" else "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .header("User-Agent", userAgents[Random.nextInt(userAgents.size)])
                        .build()
                    response = chain.proceed(request)
                    if (response.isSuccessful) return@addInterceptor response
                    Thread.sleep(1000L * (1 shl attempt)) // Exponential backoff
                } catch (e: Exception) {
                    if (attempt == 2) throw e
                }
                attempt++
            }
            response ?: throw IllegalStateException("Failed after 3 attempts")
        }
        .retryOnConnectionFailure(true)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): InstagramApiService =
        retrofit.create(InstagramApiService::class.java)

    @Provides
    @Singleton
    fun provideRemoteDataSource(apiService: InstagramApiService): InstagramRemoteDataSource =
        InstagramRemoteDataSource(apiService)
}