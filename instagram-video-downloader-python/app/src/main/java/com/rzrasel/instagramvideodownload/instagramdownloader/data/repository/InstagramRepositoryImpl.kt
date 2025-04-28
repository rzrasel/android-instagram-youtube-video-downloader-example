package com.rzrasel.instagramvideodownload.instagramdownloader.data.repository

import android.content.Context
import android.util.Log
import com.rzrasel.instagramvideodownload.core.BlobVideoDownloader
import com.rzrasel.instagramvideodownload.core.Constants
import com.rzrasel.instagramvideodownload.core.InstagramScraper
import com.rzrasel.instagramvideodownload.core.InstagramUtils
import com.rzrasel.instagramvideodownload.core.SaveVideoToStorage
import com.rzrasel.instagramvideodownload.instagramdownloader.data.datasource.InstagramRemoteDataSource
import com.rzrasel.instagramvideodownload.instagramdownloader.domain.repository.InstagramRepository
import com.rzrasel.instagramvideodownload.instagramdownloader.domain.state.DownloadState
import com.rzrasel.instagramvideodownload.instagramdownloader.domain.state.SaveState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeoutException
import javax.inject.Inject

class InstagramRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remoteDataSource: InstagramRemoteDataSource,
    private val scraper: InstagramScraper,
    private val videoSaver: SaveVideoToStorage,
    private val blobDownloader: BlobVideoDownloader
) : InstagramRepository {

    private val tag = "InstagramRepoImpl"

    private suspend fun downloadWithRetry(
        videoUrl: String,
        maxRetries: Int = 3,
        initialDelay: Long = 1000
    ): ByteArray? {
        var currentDelay = initialDelay
        repeat(maxRetries) { attempt ->
            try {
                return if (videoUrl.startsWith("blob:")) {
                    blobDownloader.downloadBlobVideo(videoUrl)
                } else {
                    downloadRegularVideo(videoUrl)
                }
            } catch (e: Exception) {
                if (attempt == maxRetries - 1) throw e
                delay(currentDelay)
                currentDelay *= 2
            }
        }
        return null
    }

    override suspend fun downloadVideo(url: String): DownloadState {
        return withContext(Dispatchers.IO) {
            try {
                validateUrl(url)?.let { return@withContext it }

                Log.d(tag, "Starting download process for URL: $url")
                var videoUrl = scraper.extractVideoUrl(url)

                if (videoUrl == null || videoUrl.startsWith("blob:")) {
                    Log.w(tag, "Client-side scraping failed or blob URL detected, trying server")
                    videoUrl = try {
                        remoteDataSource.extractVideoUrl(url)
                    } catch (e: Exception) {
                        Log.e(tag, "Server extraction failed: ${e.message}")
                        return@withContext DownloadState.Error(
                            message = "Could not extract video URL. The post may not contain a video or is private.",
                            type = DownloadState.ErrorType.NoVideoContent
                        )
                    }
                }

                if (videoUrl == null) {
                    return@withContext DownloadState.Error(
                        message = "Could not extract video URL. The post may not contain a video or is private.",
                        type = DownloadState.ErrorType.NoVideoContent
                    )
                }

                Log.d(tag, "Extracted video URL: $videoUrl")

                val videoBytes = try {
                    downloadWithRetry(videoUrl)
                } catch (e: Exception) {
                    Log.e(tag, "Download failed after retries", e)
                    return@withContext when {
                        e is TimeoutException -> DownloadState.Error(
                            message = "Download timed out. Please try again.",
                            type = DownloadState.ErrorType.NetworkError
                        )
                        videoUrl.startsWith("blob:") -> DownloadState.Error(
                            message = "Failed to process video. Blob URLs require server-side processing.",
                            type = DownloadState.ErrorType.BlobUrl
                        )
                        else -> DownloadState.Error(
                            message = "Failed to download video: ${e.message}",
                            type = DownloadState.ErrorType.NetworkError
                        )
                    }
                }

                videoBytes ?: return@withContext DownloadState.Error(
                    message = "Failed to download video content",
                    type = DownloadState.ErrorType.NetworkError
                )

                Log.d(tag, "Downloaded ${videoBytes.size} bytes, saving to storage...")
                saveVideoToStorage(videoBytes)
            } catch (e: Exception) {
                Log.e(tag, "Download error", e)
                DownloadState.Error(
                    message = e.message ?: "An unexpected error occurred",
                    type = DownloadState.ErrorType.Unknown
                )
            }
        }
    }

    private suspend fun downloadRegularVideo(videoUrl: String): ByteArray? {
        return remoteDataSource.downloadVideo(videoUrl)
    }

    private fun validateUrl(url: String): DownloadState.Error? {
        if (!InstagramUtils.isValidInstagramUrl(url)) {
            return DownloadState.Error(
                message = "Invalid Instagram URL. Must be in format: https://www.instagram.com/p/..., /reel/..., or /tv/...",
                type = DownloadState.ErrorType.InvalidUrl
            )
        }
        return null
    }

    private fun saveVideoToStorage(videoBytes: ByteArray): DownloadState {
        val fileName = "${Constants.VIDEO_FILE_PREFIX}${System.currentTimeMillis()}${Constants.VIDEO_FILE_EXTENSION}"
        return when (val result = videoSaver.save(videoBytes, fileName)) {
            is SaveState.Success -> {
                Log.d(tag, "Video saved successfully: ${result.message}")
                DownloadState.Success(result.message)
            }
            is SaveState.Error -> {
                Log.e(tag, "Failed to save video: ${result.message}")
                DownloadState.Error(
                    message = result.message,
                    type = DownloadState.ErrorType.StorageError
                )
            }
        }
    }
}