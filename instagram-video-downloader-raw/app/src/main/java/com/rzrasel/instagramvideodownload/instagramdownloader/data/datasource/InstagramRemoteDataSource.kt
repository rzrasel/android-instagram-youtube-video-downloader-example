package com.rzrasel.instagramvideodownload.instagramdownloader.data.datasource

import com.rzrasel.instagramvideodownload.instagramdownloader.data.api.InstagramApiService
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class InstagramRemoteDataSource @Inject constructor(
    private val apiService: InstagramApiService
) {
    suspend fun fetchInstagramPage(url: String): Response<ResponseBody> =
        apiService.fetchInstagramPage(url)

    suspend fun downloadVideo(videoUrl: String): Response<ResponseBody> =
        apiService.downloadVideo(videoUrl)
}