package com.rzrasel.instagramvideodownload.instagramdownloader.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Streaming
import retrofit2.http.Url

interface InstagramApiService {
    @GET
    @Headers(
        "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
        "Accept-Language: en-US,en;q=0.5",
        "Cache-Control: no-cache"
    )
    suspend fun fetchInstagramPage(@Url url: String): Response<ResponseBody>

    @GET
    @Streaming
    @Headers(
        "Accept: */*",
        "Referer: https://www.instagram.com/"
    )
    suspend fun downloadVideo(@Url videoUrl: String): Response<ResponseBody>
}