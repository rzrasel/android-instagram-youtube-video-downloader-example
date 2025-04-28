package com.rzrasel.instagramvideodownload.core

import android.util.Log
import org.json.JSONObject
import org.jsoup.Jsoup
import java.util.regex.Pattern

object InstagramUtils {
    private const val TAG = "InstagramUtils"

    fun isValidInstagramUrl(url: String): Boolean {
        val regex = Regex("""^https?://(www\.)?instagram\.com/(p|reel|tv)/[A-Za-z0-9_-]+/?(?:\?.*)?$""")
        val isValid = url.matches(regex)
        if (!isValid) {
            Log.e(TAG, "Invalid URL format: $url")
        }
        return isValid
    }

    fun extractOgVideoUrl(html: String): String? {
        return try {
            val doc = Jsoup.parse(html)
            val videoUrl = doc.select("meta[property='og:video'], meta[name='og:video']").attr("content")
            videoUrl.takeIf { it.isNotBlank() }?.also {
                Log.d(TAG, "Found og:video URL: $it")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in extractOgVideoUrl", e)
            null
        }
    }

    fun extractReelVideoUrl(html: String): String? {
        val patterns = listOf(
            Pattern.compile("\"video_url\":\"(https?:\\\\/\\\\/[^\"]+\\.mp4[^\"]+)\""),
            Pattern.compile("\"contentUrl\":\"(https?:\\\\/\\\\/[^\"]+\\.mp4[^\"]+)\""),
            Pattern.compile("\"src\":\"(https?:\\\\/\\\\/[^\"]+\\.mp4[^\"]+)\""),
            Pattern.compile("\"media\":\\{\"__typename\":\"GraphVideo\",\"video_url\":\"(https?:\\\\/\\\\/[^\"]+)\"")
        )
        for (pattern in patterns) {
            try {
                val matcher = pattern.matcher(html)
                if (matcher.find()) {
                    val url = matcher.group(1)?.replace("\\u0026", "&")?.replace("\\", "")
                    Log.d(TAG, "Found reel video URL: $url")
                    return url
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error with pattern $pattern", e)
            }
        }
        return null
    }

    fun extractVideoUrlFromJson(html: String): String? {
        return try {
            val jsonRegex = Regex("""window\._sharedData\s*=\s*(\{.*?\});""", RegexOption.DOT_MATCHES_ALL)
            val match = jsonRegex.find(html)
            match?.let {
                val json = JSONObject(it.groupValues[1])
                val media = json
                    .optJSONObject("entry_data")
                    ?.optJSONArray("PostPage")
                    ?.optJSONObject(0)
                    ?.optJSONObject("graphql")
                    ?.optJSONObject("shortcode_media")
                media?.optString("video_url")?.takeIf { it.isNotBlank() }?.also {
                    Log.d(TAG, "Found JSON video URL: $it")
                } ?: media?.optJSONArray("video_versions")
                    ?.optJSONObject(0)
                    ?.optString("url")
                    ?.takeIf { it.isNotBlank() }
                    ?.also { Log.d(TAG, "Found video_versions URL: $it") }
                ?: media?.optJSONObject("video_resources")
                    ?.optString("src")
                    ?.takeIf { it.isNotBlank() }
                    ?.also { Log.d(TAG, "Found video_resources src: $it") }
            }
        } catch (e: Exception) {
            Log.e(TAG, "JSON parsing error", e)
            null
        }
    }

    fun extractVideoUrlFromScript(html: String): String? {
        return try {
            val patterns = listOf(
                Pattern.compile("\"video_url\":\"(https?:\\\\/\\\\/[^\"]+\\.mp4[^\"]+)\""),
                Pattern.compile("\"src\":\"(https?:\\\\/\\\\/[^\"]+\\.mp4[^\"]+)\"")
            )
            for (pattern in patterns) {
                val matcher = pattern.matcher(html)
                if (matcher.find()) {
                    val url = matcher.group(1)?.replace("\\u0026", "&")?.replace("\\", "")
                    Log.d(TAG, "Found script video URL: $url")
                    return url
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting from script", e)
            null
        }
    }
}