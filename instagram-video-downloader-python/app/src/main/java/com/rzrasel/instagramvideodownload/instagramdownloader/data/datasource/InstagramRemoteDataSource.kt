package com.rzrasel.instagramvideodownload.instagramdownloader.data.datasource

import com.rzrasel.instagramvideodownload.instagramdownloader.data.api.InstagramApiService
import com.rzrasel.instagramvideodownload.instagramdownloader.data.model.VideoUrlRequest
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class InstagramRemoteDataSource @Inject constructor(
    private val apiService: InstagramApiService
) {
    suspend fun fetchInstagramPage(url: String): Response<ResponseBody> =
        apiService.fetchInstagramPage(url)

    suspend fun extractVideoUrl(url: String): String? {
        val request = VideoUrlRequest(url)
        val response = apiService.extractVideoUrl(request)
        return if (response.isSuccessful) {
            response.body()?.videoUrl
        } else {
            throw Exception(response.body()?.error ?: "Failed to extract video URL from server")
        }
    }

    suspend fun downloadVideo(videoUrl: String): ByteArray? {
        val response = apiService.downloadVideo(videoUrl)
        return if (response.isSuccessful) {
            response.body()?.bytes()
        } else {
            null
        }
    }
}