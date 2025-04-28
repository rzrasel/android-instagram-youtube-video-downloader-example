package com.rzrasel.instagramvideodownload.instagramdownloader.data.api

import com.rzrasel.instagramvideodownload.instagramdownloader.data.model.VideoUrlRequest
import com.rzrasel.instagramvideodownload.instagramdownloader.data.model.VideoUrlResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url

interface InstagramApiService {
    @GET
    @Headers(
        "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
        "Accept-Language: en-US,en;q=0.5",
        "Accept-Encoding: gzip, deflate, br",
        "Connection: keep-alive",
        "Upgrade-Insecure-Requests: 1",
        "Sec-Fetch-Dest: document",
        "Sec-Fetch-Mode: navigate",
        "Sec-Fetch-Site: none",
        "Sec-Fetch-User: ?1",
        "Cache-Control: max-age=0"
    )
    suspend fun fetchInstagramPage(@Url url: String): Response<ResponseBody>

    @POST("extract-video-url")
    suspend fun extractVideoUrl(@Body request: VideoUrlRequest): Response<VideoUrlResponse>

    @GET
    @Streaming
    @Headers(
        "Accept: */*",
        "Accept-Language: en-US,en;q=0.5",
        "Referer: https://www.instagram.com/",
        "Origin: https://www.instagram.com"
    )
    suspend fun downloadVideo(@Url videoUrl: String): Response<ResponseBody>
}