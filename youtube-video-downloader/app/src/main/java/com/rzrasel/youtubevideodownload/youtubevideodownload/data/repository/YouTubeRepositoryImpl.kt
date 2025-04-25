package com.rzrasel.youtubevideodownload.youtubevideodownload.data.repository

import android.net.Uri
import com.rzrasel.youtubevideodownload.core.YouTubeDownloader
import com.rzrasel.youtubevideodownload.core.YouTubeScraper
import com.rzrasel.youtubevideodownload.youtubevideodownload.domain.model.YouTubeStream
import com.rzrasel.youtubevideodownload.youtubevideodownload.domain.repository.YouTubeRepository
import javax.inject.Inject

class YouTubeRepositoryImpl @Inject constructor(
    private val scraper: YouTubeScraper,
    private val downloader: YouTubeDownloader
) : YouTubeRepository {

    override suspend fun getVideoStreams(videoUrl: String): List<YouTubeStream> {
        return try {
            val streams = scraper.getVideoStreams(videoUrl)
            if (streams.isEmpty()) {
                throw Exception("No streams found for this video")
            }
            streams
        } catch (e: Exception) {
            throw Exception("Failed to get video streams: ${e.message}")
        }
    }

    override suspend fun downloadVideo(
        url: String,
        fileName: String,
        hasPermission: Boolean
    ): Uri? {
        return try {
            downloader.downloadVideo(url, fileName, hasPermission) {
                throw Exception("Storage permission denied")
            }
        } catch (e: Exception) {
            throw Exception("Download failed: ${e.message}")
        }
    }
}