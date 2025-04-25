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

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
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