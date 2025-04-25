package com.rzrasel.youtubevideodownload.youtubevideodownload.data.datasource

import com.rzrasel.youtubevideodownload.youtubevideodownload.data.api.YouTubeApiService
import javax.inject.Inject

class YouTubeRemoteDataSource @Inject constructor(
    private val apiService: YouTubeApiService
) {
    suspend fun getVideoInfoDirect(videoId: String) = apiService.getVideoInfoDirect(videoId)
}