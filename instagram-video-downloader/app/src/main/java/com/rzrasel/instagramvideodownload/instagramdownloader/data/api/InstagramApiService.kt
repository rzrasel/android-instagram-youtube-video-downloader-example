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
        "User-Agent: Mozilla/5.0 (Linux; Android 10; SM-G975F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36",
        "Accept-Language: en-US,en;q=0.9",
        "Referer: https://www.instagram.com/",
        "X-Requested-With: XMLHttpRequest"
    )
    suspend fun fetchInstagramPage(@Url url: String): Response<ResponseBody>

    @GET
    @Streaming
    suspend fun downloadVideo(@Url videoUrl: String): Response<ResponseBody>
}