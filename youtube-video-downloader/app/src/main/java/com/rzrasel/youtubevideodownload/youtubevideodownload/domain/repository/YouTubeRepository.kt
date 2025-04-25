package com.rzrasel.youtubevideodownload.youtubevideodownload.domain.repository

import android.net.Uri
import com.rzrasel.youtubevideodownload.youtubevideodownload.domain.model.YouTubeStream

interface YouTubeRepository {
    suspend fun getVideoStreams(videoUrl: String): List<YouTubeStream>
    suspend fun downloadVideo(url: String, fileName: String, hasPermission: Boolean): Uri?
}