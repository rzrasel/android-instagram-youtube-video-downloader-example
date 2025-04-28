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
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import okhttp3.internal.closeQuietly
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class InstagramRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remoteDataSource: InstagramRemoteDataSource,
    private val scraper: InstagramScraper,
    private val videoSaver: SaveVideoToStorage,
    private val blobDownloader: BlobVideoDownloader
) : InstagramRepository {

    private val tag = "InstagramRepoImpl"

    override suspend fun downloadVideo(url: String): DownloadState {
        return withContext(Dispatchers.IO) {
            try {
                validateUrl(url)?.let { return@withContext it }

                Log.d(tag, "Starting download process for URL: $url")
                val videoUrl = scraper.extractVideoUrl(url)
                    ?: return@withContext DownloadState.Error("Video content not found. The post may not contain a video or is private.")

                Log.d(tag, "Extracted video URL: $videoUrl")

                val videoBytes = if (videoUrl.startsWith("blob:")) {
                    Log.d(tag, "Downloading blob video")
                    try {
                        blobDownloader.downloadBlobVideo(videoUrl)
                    } catch (e: Exception) {
                        Log.e(tag, "Blob download failed", e)
                        null
                    }
                } else {
                    downloadRegularVideo(videoUrl)
                }

                videoBytes ?: return@withContext DownloadState.Error("Failed to download video. Check your network connection.")

                Log.d(tag, "Downloaded ${videoBytes.size} bytes, saving to storage...")
                saveVideoToStorage(videoBytes)
            } catch (e: Exception) {
                Log.e(tag, "Download error", e)
                DownloadState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    private suspend fun downloadRegularVideo(videoUrl: String): ByteArray? {
        var response: Response<ResponseBody>? = null
        return try {
            Log.d(tag, "Downloading regular video from: $videoUrl")
            response = remoteDataSource.downloadVideo(videoUrl)

            if (!response.isSuccessful) {
                Log.e(tag, "Download failed: ${response.code()} - ${response.message()}")
                return null
            }

            val contentLength = response.body()?.contentLength() ?: -1L
            if (contentLength > Constants.MAX_VIDEO_SIZE_BYTES) {
                Log.e(tag, "Video too large: $contentLength bytes (max ${Constants.MAX_VIDEO_SIZE_BYTES})")
                return null
            }

            Log.d(tag, "Content length: $contentLength bytes")
            response.body()?.bytes()
        } catch (e: IOException) {
            Log.e(tag, "Network error downloading video", e)
            null
        } finally {
            response?.body()?.closeQuietly()
        }
    }

    private fun validateUrl(url: String): DownloadState.Error? {
        if (!InstagramUtils.isValidInstagramUrl(url)) {
            return DownloadState.Error("Invalid Instagram URL. Must be in format: https://www.instagram.com/p/..., /reel/..., or /tv/...")
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
                DownloadState.Error(result.message)
            }
        }
    }
}