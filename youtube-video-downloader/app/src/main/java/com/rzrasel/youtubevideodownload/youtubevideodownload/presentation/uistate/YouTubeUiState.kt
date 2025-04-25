package com.rzrasel.youtubevideodownload.youtubevideodownload.presentation.uistate

import com.rzrasel.youtubevideodownload.youtubevideodownload.domain.model.YouTubeStream
import com.rzrasel.youtubevideodownload.youtubevideodownload.domain.networkstate.NetworkState

data class YouTubeUiState(
    val url: String = "",
    val streams: List<YouTubeStream> = emptyList(),
    val error: String? = null,
    val networkState: NetworkState = NetworkState.Idle,
    val downloadingUrl: String? = null,
    val isFetchingStreams: Boolean = false,
    val isDownloading: Boolean = false
)
