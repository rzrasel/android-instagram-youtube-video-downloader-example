package com.rzrasel.youtubevideodownload.youtubevideodownload.data.model

import com.rzrasel.youtubevideodownload.youtubevideodownload.domain.model.YouTubeStream

data class YouTubeStreamDto(
    val url: String,
    val quality: String,
    val resolution: String,
    val mimeType: String,
    val bitrate: Int
) {
    fun toDomain() = YouTubeStream(url, quality, resolution, mimeType, bitrate)
}