package com.rzrasel.youtubevideodownload.youtubevideodownload.di

import android.content.Context
import com.rzrasel.youtubevideodownload.core.YouTubeDownloader
import com.rzrasel.youtubevideodownload.core.YouTubeScraper
import com.rzrasel.youtubevideodownload.youtubevideodownload.data.api.ApiClient
import com.rzrasel.youtubevideodownload.youtubevideodownload.data.api.YouTubeApiService
import com.rzrasel.youtubevideodownload.youtubevideodownload.data.datasource.YouTubeRemoteDataSource
import com.rzrasel.youtubevideodownload.youtubevideodownload.data.repository.YouTubeRepositoryImpl
import com.rzrasel.youtubevideodownload.youtubevideodownload.domain.repository.YouTubeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideApiClient(): ApiClient {
        return ApiClient()
    }

    @Provides
    @Singleton
    fun provideYouTubeApiService(apiClient: ApiClient): YouTubeApiService {
        return apiClient.youTubeApiService
    }

    @Provides
    @Singleton
    fun provideYouTubeRemoteDataSource(apiService: YouTubeApiService): YouTubeRemoteDataSource {
        return YouTubeRemoteDataSource(apiService)
    }

    @Provides
    @Singleton
    fun provideYouTubeScraper(apiService: YouTubeApiService): YouTubeScraper {
        return YouTubeScraper(apiService)
    }

    @Provides
    @Singleton
    fun provideYouTubeDownloader(@ApplicationContext context: Context): YouTubeDownloader {
        return YouTubeDownloader(context)
    }

    @Provides
    @Singleton
    fun provideYouTubeRepository(
        scraper: YouTubeScraper,
        downloader: YouTubeDownloader
    ): YouTubeRepository {
        return YouTubeRepositoryImpl(scraper, downloader)
    }
}