package com.rzrasel.instagramvideodownload.core

import android.util.Log
import com.rzrasel.instagramvideodownload.instagramdownloader.data.datasource.InstagramRemoteDataSource
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.json.JSONObject
import javax.inject.Inject

class InstagramScraper @Inject constructor(
    private val remoteDataSource: InstagramRemoteDataSource
) {
    private val tag = "InstagramScraper"

    suspend fun extractVideoUrl(pageUrl: String): String? {
        return try {
            tryJsonApi(pageUrl)?.let { return it }

            tryHtmlParsing(pageUrl)?.let { return it }

            tryMobilePage(pageUrl)
        } catch (e: Exception) {
            Log.e(tag, "Error extracting video URL", e)
            null
        }
    }

    private suspend fun tryJsonApi(pageUrl: String): String? {
        val jsonUrl = "${InstagramUtils.cleanInstagramUrl(pageUrl)}?__a=1&__d=dis"
        val jsonResponse = remoteDataSource.fetchInstagramPage(jsonUrl)

        if (!jsonResponse.isSuccessful) return null

        return jsonResponse.body()?.string()?.let { jsonBody ->
            parseVideoUrlFromJson(jsonBody)
        }
    }

    private suspend fun tryHtmlParsing(pageUrl: String): String? {
        val htmlResponse = remoteDataSource.fetchInstagramPage(pageUrl)
        if (!htmlResponse.isSuccessful) {
            Log.e(tag, "HTML fetch failed: ${htmlResponse.code()}")
            return null
        }

        return htmlResponse.body()?.string()?.let { html ->
            InstagramUtils.extractVideoUrlFromHtml(html)
        }
    }

    private suspend fun tryMobilePage(pageUrl: String): String? {
        val mobileUrl = pageUrl.replace("www.instagram.com", "m.instagram.com")
        val mobileResponse = remoteDataSource.fetchInstagramPage(mobileUrl)
        if (!mobileResponse.isSuccessful) return null

        return mobileResponse.body()?.string()?.let { html ->
            InstagramUtils.extractVideoUrlFromHtml(html)
        }
    }

    private fun parseVideoUrlFromJson(jsonString: String): String? {
        return try {
            if (jsonString.contains("\"is_private\":true")) {
                Log.e(tag, "Private account content cannot be downloaded")
                return null
            }

            val root = JSONObject(jsonString)

            // Try different JSON structures
            listOf(
                root.optJSONObject("graphql")?.optJSONObject("shortcode_media"),
                root.optJSONArray("items")?.optJSONObject(0),
                root.optJSONObject("data")?.optJSONObject("shortcode_media")
            ).firstNotNullOfOrNull { media ->
                extractVideoUrlFromMedia(media)
            }
        } catch (e: Exception) {
            Log.e(tag, "JSON parsing error", e)
            null
        }
    }

    private fun extractVideoUrlFromMedia(media: JSONObject?): String? {
        if (media == null) return null

        return listOf(
            media.optString("video_url").takeIf { it.isNotBlank() },
            media.optJSONArray("video_versions")
                ?.optJSONObject(0)
                ?.optString("url")
                ?.takeIf { it.isNotBlank() },
            media.optJSONObject("video_resources")
                ?.optString("src")
                ?.takeIf { it.isNotBlank() }
        ).firstNotNullOfOrNull { url ->
            url?.also { Log.d(tag, "Found video URL in JSON: $it") }
        }
    }
}