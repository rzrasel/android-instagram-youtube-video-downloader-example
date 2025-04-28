package com.rzrasel.instagramvideodownload.instagramdownloader.data.repository

import android.util.Log
import com.rzrasel.instagramvideodownload.core.*
import com.rzrasel.instagramvideodownload.instagramdownloader.data.datasource.InstagramRemoteDataSource
import com.rzrasel.instagramvideodownload.instagramdownloader.domain.repository.InstagramRepository
import com.rzrasel.instagramvideodownload.instagramdownloader.domain.state.DownloadState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import okhttp3.internal.closeQuietly
import retrofit2.Response
import javax.inject.Inject

class InstagramRepositoryImpl @Inject constructor(
    private val remoteDataSource: InstagramRemoteDataSource,
    private val scraper: InstagramScraper,
    private val videoSaver: SaveVideoToStorage,
    private val blobDownloader: BlobVideoDownloader
) : InstagramRepository {

    private val tag = "InstagramRepo"

    override suspend fun downloadVideo(url: String): DownloadState {
        return withContext(Dispatchers.IO) {
            try {
                validateUrl(url)?.let { return@withContext it }

                Log.d(tag, "Starting download for URL: $url")
                val videoUrl = scraper.extractVideoUrl(url)
                    ?: return@withContext DownloadState.Error(
                        "Could not extract video URL",
                        DownloadState.ErrorType.NoVideoContent
                    )

                Log.d(tag, "Extracted video URL: $videoUrl")
                val videoBytes = downloadWithRetry(videoUrl)
                    ?: return@withContext DownloadState.Error(
                        "Failed to download video content",
                        DownloadState.ErrorType.NetworkError
                    )

                Log.d(tag, "Downloaded ${videoBytes.size} bytes, saving...")
                saveVideoToStorage(videoBytes)
            } catch (e: Exception) {
                Log.e(tag, "Download error", e)
                DownloadState.Error(
                    e.message ?: "An unexpected error occurred",
                    DownloadState.ErrorType.fromException(e)
                )
            }
        }
    }

    private suspend fun downloadWithRetry(
        videoUrl: String,
        maxRetries: Int = Constants.MAX_RETRIES,
        initialDelay: Long = Constants.INITIAL_RETRY_DELAY
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
                kotlinx.coroutines.delay(currentDelay)
                currentDelay *= 2
            }
        }
        return null
    }

    private suspend fun downloadRegularVideo(videoUrl: String): ByteArray? {
        var response: Response<ResponseBody>? = null
        return try {
            response = remoteDataSource.downloadVideo(videoUrl)

            if (!response.isSuccessful) {
                Log.e(tag, "Download failed: ${response.code()}")
                return null
            }

            response.body()?.bytes()
        } catch (e: Exception) {
            Log.e(tag, "Network error downloading video", e)
            null
        } finally {
            response?.body()?.closeQuietly()
        }
    }

    private fun validateUrl(url: String): DownloadState.Error? {
        if (!InstagramUtils.isValidInstagramUrl(url)) {
            return DownloadState.Error(
                "Invalid Instagram URL format",
                DownloadState.ErrorType.InvalidUrl
            )
        }
        return null
    }

    private fun saveVideoToStorage(videoBytes: ByteArray): DownloadState {
        val fileName = "${Constants.VIDEO_FILE_PREFIX}${System.currentTimeMillis()}${Constants.VIDEO_FILE_EXTENSION}"
        return when (val result = videoSaver.save(videoBytes, fileName)) {
            is SaveVideoToStorage.SaveResult.Success -> {
                Log.d(tag, result.message)
                DownloadState.Success(result.message)
            }
            is SaveVideoToStorage.SaveResult.Error -> {
                Log.e(tag, result.message)
                DownloadState.Error(result.message, DownloadState.ErrorType.StorageError)
            }
        }
    }
}