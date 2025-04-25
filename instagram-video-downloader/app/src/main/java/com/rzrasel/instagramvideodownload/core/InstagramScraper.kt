package com.rzrasel.instagramvideodownload.core

import android.util.Log
import com.rzrasel.instagramvideodownload.instagramdownloader.data.datasource.InstagramRemoteDataSource
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class InstagramScraper @Inject constructor(
    private val remoteDataSource: InstagramRemoteDataSource
) {
    private val tag = "InstagramScraper"

    suspend fun extractVideoUrl(pageUrl: String): String? {
        return try {
            val response: Response<ResponseBody> = remoteDataSource.fetchInstagramPage(pageUrl)
            if (!response.isSuccessful || response.body() == null) return null

            val html = response.body()!!.string()
            InstagramUtils.extractOgVideoUrl(html)
                ?: InstagramUtils.extractReelVideoUrl(html)
                ?: InstagramUtils.extractVideoUrlFromJson(html)
        } catch (e: Exception) {
            Log.e(tag, "Error extracting video URL", e)
            null
        }
    }
}