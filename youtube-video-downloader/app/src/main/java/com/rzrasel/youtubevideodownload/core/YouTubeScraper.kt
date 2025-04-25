package com.rzrasel.youtubevideodownload.core

import com.rzrasel.youtubevideodownload.youtubevideodownload.data.api.YouTubeApiService
import com.rzrasel.youtubevideodownload.youtubevideodownload.domain.model.YouTubeStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import javax.inject.Inject

class YouTubeScraper @Inject constructor(
    private val apiService: YouTubeApiService
) {
    suspend fun getVideoStreams(videoUrl: String): List<YouTubeStream> {
        return withContext(Dispatchers.IO) {
            val videoId = YouTubeUtils.extractVideoId(videoUrl)
                ?: throw Exception("Invalid YouTube URL")

            // First try the direct API approach
            val apiResult = runCatching {
                val response = apiService.getVideoInfoDirect(videoId)
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        return@withContext parseStreamsFromVideoInfo(body)
                    }
                }
                throw Exception("API response unsuccessful")
            }

            // If API approach failed, try HTML parsing
            val htmlResult = runCatching {
                val page = Jsoup.connect(YouTubeUtils.getVideoInfoUrl(videoId))
                    .userAgent("Mozilla/5.0")
                    .get()
                    .html()

                val jsonRegex = Regex("var ytInitialPlayerResponse\\s*=\\s*(\\{.*?\\});")
                val match = jsonRegex.find(page) ?: throw Exception("Player JSON not found")
                val json = match.groupValues[1]

                parseStreamsFromJson(JSONObject(json))
            }

            // If both approaches failed, throw the last exception
            htmlResult.getOrElse { htmlError ->
                apiResult.fold(
                    onSuccess = { throw Exception("Unexpected state") }, // Shouldn't happen
                    onFailure = { apiError ->
                        throw Exception(
                            "Failed to fetch streams:\n" +
                                    "API Error: ${apiError.message}\n" +
                                    "HTML Error: ${htmlError.message}"
                        )
                    }
                )
            }
        }
    }

    private fun parseStreamsFromVideoInfo(videoInfo: String): List<YouTubeStream> {
        val streams = mutableListOf<YouTubeStream>()
        val params = videoInfo.split('&')

        try {
            val urlEncodedStreams = params.firstOrNull { it.startsWith("url_encoded_fmt_stream_map=") }
                ?.substringAfter("url_encoded_fmt_stream_map=")
                ?.let { java.net.URLDecoder.decode(it, "UTF-8") }
                ?: return emptyList()

            urlEncodedStreams.split(',').forEach { streamData ->
                try {
                    val streamParams = streamData.split('&').associate {
                        val parts = it.split('=')
                        parts[0] to parts.getOrNull(1).orEmpty()
                    }

                    val url = java.net.URLDecoder.decode(streamParams["url"].orEmpty(), "UTF-8")
                    val quality = streamParams["quality"] ?: "unknown"
                    val type = streamParams["type"] ?: ""
                    val width = streamParams["width"]?.toIntOrNull() ?: 0
                    val height = streamParams["height"]?.toIntOrNull() ?: 0

                    if (url.isNotEmpty() && type.contains("video/mp4")) {
                        streams.add(
                            YouTubeStream(
                                url = url,
                                quality = quality,
                                resolution = "${width}x${height}",
                                mimeType = type,
                                bitrate = streamParams["bitrate"]?.toIntOrNull() ?: 0
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Skip invalid streams
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to parse video info: ${e.message}")
        }

        return streams
    }

    private fun parseStreamsFromJson(response: JSONObject): List<YouTubeStream> {
        val streamingData = response.getJSONObject("streamingData")
        val formats = mutableListOf<JSONObject>()

        streamingData.optJSONArray("formats")?.let {
            formats += 0.until(it.length()).map { i -> it.getJSONObject(i) }
        }

        streamingData.optJSONArray("adaptiveFormats")?.let {
            formats += 0.until(it.length()).map { i -> it.getJSONObject(i) }
        }

        return formats.mapNotNull { format ->
            val mimeType = format.optString("mimeType")
            val url = format.optString("url")

            if (url.isNotEmpty() && mimeType.contains("video/mp4")) {
                YouTubeStream(
                    url = url,
                    quality = format.optString("qualityLabel", "Unknown"),
                    resolution = "${format.optInt("width")}x${format.optInt("height")}",
                    mimeType = mimeType,
                    bitrate = format.optInt("bitrate", 0)
                )
            } else null
        }.sortedByDescending { it.quality }
    }
}