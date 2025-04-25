package com.rzrasel.youtubevideodownload.youtubevideodownload.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import retrofit2.http.Url

interface YouTubeApiService {
    @GET
    suspend fun getVideoInfo(@Url url: String): Response<ResponseBody>

    @GET("get_video_info")
    @Headers("User-Agent: Mozilla/5.0")
    suspend fun getVideoInfoDirect(
        @Query("video_id") videoId: String,
        @Query("el") el: String = "embedded",
        @Query("ps") ps: String = "default"
    ): Response<String>
}