package com.rzrasel.instagramvideodownload.core

import android.util.Log

object InstagramUtils {
    private const val TAG = "InstagramUtils"

    fun isValidInstagramUrl(url: String): Boolean {
        return Constants.INSTAGRAM_URL_REGEX.matches(url).also {
            if (!it) Log.w(TAG, "Invalid Instagram URL: $url")
        }
    }

    fun cleanInstagramUrl(url: String): String {
        return url.split("?").firstOrNull()?.trimEnd('/') ?: url
    }

    fun extractVideoUrlFromHtml(html: String): String? {
        return try {
            // Try to find video URL in known patterns
            Constants.VIDEO_URL_PATTERNS.firstNotNullOfOrNull { pattern ->
                pattern.find(html)?.let { match ->
                    match.groupValues[1]
                        .replace("\\u0026", "&")
                        .replace("\\", "")
                        .takeIf { it.isNotBlank() }
                        ?.also { Log.d(TAG, "Found video URL with pattern: $pattern") }
                }
            } ?: extractBlobVideoUrl(html)
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting video URL from HTML", e)
            null
        }
    }

    private fun extractBlobVideoUrl(html: String): String? {
        return html.substringAfter("blob:")
            .takeIf { it != html }
            ?.let { "blob:$it" }
            ?.also { Log.d(TAG, "Found blob video URL") }
    }
}