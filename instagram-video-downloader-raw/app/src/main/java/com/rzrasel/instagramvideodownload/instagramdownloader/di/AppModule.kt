package com.rzrasel.instagramvideodownload.instagramdownloader.di

import android.content.Context
import com.rzrasel.instagramvideodownload.core.*
import com.rzrasel.instagramvideodownload.instagramdownloader.data.datasource.InstagramRemoteDataSource
import com.rzrasel.instagramvideodownload.instagramdownloader.data.repository.InstagramRepositoryImpl
import com.rzrasel.instagramvideodownload.instagramdownloader.domain.repository.InstagramRepository
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
    fun provideInstagramScraper(
        remoteDataSource: InstagramRemoteDataSource
    ): InstagramScraper = InstagramScraper(remoteDataSource)

    @Provides
    @Singleton
    fun provideInstagramUtils(): InstagramUtils = InstagramUtils

    @Provides
    @Singleton
    fun provideSaveVideoToStorage(
        @ApplicationContext context: Context
    ): SaveVideoToStorage = SaveVideoToStorage(context)

    @Provides
    @Singleton
    fun provideBlobVideoDownloader(
        @ApplicationContext context: Context
    ): BlobVideoDownloader = BlobVideoDownloader(context)

    @Provides
    @Singleton
    fun provideInstagramRepository(
        @ApplicationContext context: Context,
        remoteDataSource: InstagramRemoteDataSource,
        scraper: InstagramScraper,
        videoSaver: SaveVideoToStorage,
        blobDownloader: BlobVideoDownloader
    ): InstagramRepository {
        return InstagramRepositoryImpl(
            context,
            remoteDataSource,
            scraper,
            videoSaver,
            blobDownloader
        )
    }
}