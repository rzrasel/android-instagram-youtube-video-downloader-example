package com.rzrasel.instagramvideodownload.core

import android.util.Log
import com.rzrasel.instagramvideodownload.instagramdownloader.data.datasource.InstagramRemoteDataSource
import org.json.JSONObject
import org.jsoup.Jsoup
import javax.inject.Inject

class InstagramScraper @Inject constructor(
    private val remoteDataSource: InstagramRemoteDataSource
) {
    private val tag = "InstagramScraper"

    suspend fun extractVideoUrl(pageUrl: String): String? {
        return try {
            repeat(3) { attempt ->
                Log.d(tag, "Attempt ${attempt + 1} for page: $pageUrl")
                val cleanedUrl = cleanInstagramUrl(pageUrl)
                val jsonUrl = "$cleanedUrl?__a=1&__d=dis"

                val jsonResponse = remoteDataSource.fetchInstagramPage(jsonUrl)
                if (jsonResponse.isSuccessful) {
                    val jsonBody = jsonResponse.body()?.string() ?: return@repeat
                    parseVideoUrlFromJson(jsonBody)?.let { return it }
                }

                val htmlResponse = remoteDataSource.fetchInstagramPage(cleanedUrl)
                if (!htmlResponse.isSuccessful) {
                    Log.e(tag, "HTML fetch failed: ${htmlResponse.code()}")
                    return@repeat
                }

                val html = htmlResponse.body()?.string() ?: return@repeat
                Log.d(tag, "Fetched HTML content, length: ${html.length}")

                val methods = listOf(
                    "extractOgVideoUrl" to InstagramUtils.extractOgVideoUrl(html),
                    "extractReelVideoUrl" to InstagramUtils.extractReelVideoUrl(html),
                    "extractVideoUrlFromJson" to InstagramUtils.extractVideoUrlFromJson(html),
                    "extractVideoUrlFromScript" to InstagramUtils.extractVideoUrlFromScript(html),
                    "extractBlobVideoUrl" to extractBlobVideoUrl(html)
                )

                methods.forEach { (method, result) ->
                    Log.d(tag, "$method result: $result")
                }

                methods.mapNotNull { it.second }.firstOrNull()?.let { return it }
            }
            Log.e(tag, "All attempts failed")
            null
        } catch (e: Exception) {
            Log.e(tag, "Error extracting video URL", e)
            null
        }
    }

    private fun extractBlobVideoUrl(html: String): String? {
        return try {
            val doc = Jsoup.parse(html)
            val videoElements = doc.select("video[src^=blob:]")
            videoElements.firstOrNull()?.attr("src")?.also {
                Log.d(tag, "Found blob video URL: $it")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error extracting blob video", e)
            null
        }
    }

    private fun cleanInstagramUrl(url: String): String {
        return url.split("?").firstOrNull()?.trimEnd('/') ?: url
    }

    private fun parseVideoUrlFromJson(jsonString: String): String? {
        return try {
            if (jsonString.contains("\"is_private\":true")) {
                Log.e(tag, "Cannot download from private account")
                return null
            }
            val root = JSONObject(jsonString)
            root.optJSONObject("graphql")?.let { graphql ->
                val media = graphql.optJSONObject("shortcode_media")
                return extractVideoUrlFromMedia(media)
            }
            root.optJSONArray("items")?.let { items ->
                if (items.length() > 0) {
                    val media = items.optJSONObject(0)
                    return extractVideoUrlFromMedia(media)
                }
            }
            root.optJSONObject("data")?.optJSONObject("shortcode_media")?.let { media ->
                return extractVideoUrlFromMedia(media)
            }
            Log.w(tag, "No video URL found in JSON")
            null
        } catch (e: Exception) {
            Log.e(tag, "Error parsing JSON", e)
            null
        }
    }

    private fun extractVideoUrlFromMedia(media: JSONObject?): String? {
        if (media == null) return null

        media.optString("video_url").takeIf { it.isNotBlank() }?.let {
            Log.d(tag, "Found video_url: $it")
            return it
        }

        media.optJSONArray("video_versions")?.let { versions ->
            if (versions.length() > 0) {
                val url = versions.optJSONObject(0)?.optString("url")?.takeIf { it.isNotBlank() }
                Log.d(tag, "Found video_versions URL: $url")
                return url
            }
        }

        media.optJSONObject("video_resources")?.optString("src")?.takeIf { it.isNotBlank() }?.let {
            Log.d(tag, "Found video_resources src: $it")
            return it
        }

        return null
    }
}