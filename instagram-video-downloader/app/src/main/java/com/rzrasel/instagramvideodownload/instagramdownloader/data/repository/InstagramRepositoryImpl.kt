package com.rzrasel.instagramvideodownload.instagramdownloader.data.repository

import android.content.Context
import android.util.Log
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
import okhttp3.internal.closeQuietly
import java.io.InputStream
import javax.inject.Inject

class InstagramRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remoteDataSource: InstagramRemoteDataSource,
    private val scraper: InstagramScraper,
    private val videoSaver: SaveVideoToStorage
) : InstagramRepository {

    private val tag = "InstagramRepoImpl"

    override suspend fun downloadVideo(url: String): DownloadState {
        return withContext(Dispatchers.IO) {
            try {
                validateUrl(url)?.let { return@withContext it }

                val videoUrl = scraper.extractVideoUrl(url)
                    ?: return@withContext DownloadState.Error("Video content not found")

                Log.d(tag, "Extracted video URL: $videoUrl")

                val videoBytes = downloadVideoBytes(videoUrl)
                    ?: return@withContext DownloadState.Error("Failed to download video")

                saveVideoToStorage(videoBytes)
            } catch (e: Exception) {
                Log.e(tag, "Download error", e)
                DownloadState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun validateUrl(url: String): DownloadState.Error? {
        if (!InstagramUtils.isValidInstagramUrl(url)) {
            return DownloadState.Error("Invalid Instagram URL")
        }
        return null
    }

    private suspend fun downloadVideoBytes(videoUrl: String): ByteArray? {
        var inputStream: InputStream? = null
        return try {
            val response = remoteDataSource.downloadVideo(videoUrl)
            if (!response.isSuccessful || response.body() == null) {
                Log.e(tag, "Download failed: ${response.code()} - ${response.message()}")
                return null
            }
            inputStream = response.body()!!.byteStream()
            inputStream.readBytes()
        } catch (e: Exception) {
            Log.e(tag, "Download bytes error", e)
            null
        } finally {
            inputStream?.closeQuietly()
        }
    }

    private fun saveVideoToStorage(videoBytes: ByteArray): DownloadState {
        val fileName = "${Constants.VIDEO_FILE_PREFIX}${System.currentTimeMillis()}${Constants.VIDEO_FILE_EXTENSION}"
        return when (val result = videoSaver.save(videoBytes, fileName)) {
            is SaveState.Success -> DownloadState.Success(result.message)
            is SaveState.Error -> DownloadState.Error(result.message)
        }
    }
}