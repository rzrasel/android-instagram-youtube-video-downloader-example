package com.rzrasel.instagramvideodownload.core

import android.util.Log
import org.json.JSONObject
import org.jsoup.Jsoup
import java.util.regex.Pattern

object InstagramUtils {
    private const val TAG = "InstagramUtils"

    fun isValidInstagramUrl(url: String): Boolean {
        return url.matches(Regex("""^https?://(www\.)?instagram\.com/(p|reel|tv)/[A-Za-z0-9_-]+/?(?:\?.*)?$"""))
    }

    fun extractOgVideoUrl(html: String): String? {
        val doc = Jsoup.parse(html)
        return doc.select("meta[property='og:video']").attr("content").takeIf { it.isNotBlank() }
    }

    fun extractReelVideoUrl(html: String): String? {
        val pattern = Pattern.compile("video_url\":\"(.*?)\"")
        val matcher = pattern.matcher(html)
        return if (matcher.find()) matcher.group(1)?.replace("\\u0026", "&") else null
    }

    fun extractVideoUrlFromJson(html: String): String? {
        return try {
            val jsonRegex = Regex("""window\._sharedData = (\{.*?\});</script>""")
            val match = jsonRegex.find(html)
            match?.let {
                JSONObject(it.groupValues[1])
                    .optJSONObject("entry_data")
                    ?.optJSONArray("PostPage")
                    ?.optJSONObject(0)
                    ?.optJSONObject("graphql")
                    ?.optJSONObject("shortcode_media")
                    ?.let { media ->
                        media.optString("video_url").takeIf { it.isNotBlank() }
                            ?: media.optJSONArray("video_versions")
                                ?.optJSONObject(0)
                                ?.optString("url")
                                ?.takeIf { it.isNotBlank() }
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "JSON parsing error", e)
            null
        }
    }
}