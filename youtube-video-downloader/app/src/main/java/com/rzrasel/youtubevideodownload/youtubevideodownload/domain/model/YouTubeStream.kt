package com.rzrasel.youtubevideodownload.youtubevideodownload.domain.model

data class YouTubeStream(
    val url: String,
    val quality: String,
    val resolution: String,
    val mimeType: String,
    val bitrate: Int
)