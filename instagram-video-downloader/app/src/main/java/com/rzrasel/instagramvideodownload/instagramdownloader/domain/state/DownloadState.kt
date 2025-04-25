package com.rzrasel.instagramvideodownload.instagramdownloader.domain.state

sealed class DownloadState {
    data object Loading : DownloadState()
    data class Success(val message: String) : DownloadState()
    data class Error(val message: String) : DownloadState()
}