package com.rzrasel.youtubevideodownload.core

import android.net.Uri

object YouTubeUtils {
    private val YOUTUBE_DOMAINS = listOf(
        "youtube.com",
        "www.youtube.com",
        "m.youtube.com",
        "youtu.be",
        "www.youtu.be"
    )

    fun isValidYouTubeUrl(url: String): Boolean {
        return try {
            val uri = Uri.parse(url)
            val host = uri.host?.lowercase() ?: return false
            YOUTUBE_DOMAINS.any { domain -> host.contains(domain) }
        } catch (e: Exception) {
            false
        }
    }

    fun extractVideoId(url: String): String? {
        val patterns = listOf(
            "(?:v=|youtu\\.be/|/embed/|/v/|/e/|watch\\?v=)([a-zA-Z0-9_-]{11})",
            "^([a-zA-Z0-9_-]{11})\$"
        )

        for (pattern in patterns) {
            val regex = Regex(pattern)
            val match = regex.find(url) ?: continue
            return match.groupValues[1]
        }
        return null
    }

    fun getVideoInfoUrl(videoId: String): String {
        return "https://www.youtube.com/watch?v=$videoId"
    }
}