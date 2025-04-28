package com.rzrasel.instagramvideodownload.instagramdownloader.presentation.uistate

import com.rzrasel.instagramvideodownload.instagramdownloader.domain.state.DownloadState

sealed class InstagramUiState {
    data object Idle : InstagramUiState()
    data object Loading : InstagramUiState()
    data class Success(val message: String) : InstagramUiState()
    data class Error(val message: String, val type: DownloadState.ErrorType) : InstagramUiState()
}
