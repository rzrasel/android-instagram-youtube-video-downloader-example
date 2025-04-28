package com.rzrasel.instagramvideodownload.instagramdownloader.presentation.uistate

sealed class InstagramUiState {
    data object Idle : InstagramUiState()
    data object Loading : InstagramUiState()
    data class DownloadSuccess(val message: String) : InstagramUiState()
    data class DownloadError(val message: String, val type: ErrorType = ErrorType.Unknown) : InstagramUiState()

    enum class ErrorType {
        InvalidUrl, PrivateAccount, NoVideoContent, NetworkError, BlobUrl, Unknown
    }
}