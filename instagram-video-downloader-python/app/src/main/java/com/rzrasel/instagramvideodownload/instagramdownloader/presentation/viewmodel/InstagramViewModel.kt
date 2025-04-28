package com.rzrasel.instagramvideodownload.instagramdownloader.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rzrasel.instagramvideodownload.instagramdownloader.domain.state.DownloadState
import com.rzrasel.instagramvideodownload.instagramdownloader.domain.usecase.DownloadInstagramVideoUseCase
import com.rzrasel.instagramvideodownload.instagramdownloader.presentation.uistate.InstagramUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InstagramViewModel @Inject constructor(
    private val downloadUseCase: DownloadInstagramVideoUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<InstagramUiState>(InstagramUiState.Idle)
    val uiState: StateFlow<InstagramUiState> = _uiState

    private var currentJob: Job? = null

    fun downloadVideo(url: String) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            _uiState.value = InstagramUiState.Loading
            try {
                when (val result = downloadUseCase(url)) {
                    is DownloadState.Success -> {
                        _uiState.value = InstagramUiState.DownloadSuccess(result.message)
                    }
                    is DownloadState.Error -> {
                        val errorType = when (result.type) {
                            DownloadState.ErrorType.InvalidUrl -> InstagramUiState.ErrorType.InvalidUrl
                            DownloadState.ErrorType.PrivateAccount -> InstagramUiState.ErrorType.PrivateAccount
                            DownloadState.ErrorType.NoVideoContent -> InstagramUiState.ErrorType.NoVideoContent
                            DownloadState.ErrorType.NetworkError -> InstagramUiState.ErrorType.NetworkError
                            DownloadState.ErrorType.BlobUrl -> InstagramUiState.ErrorType.BlobUrl
                            DownloadState.ErrorType.StorageError -> InstagramUiState.ErrorType.StorageError
                            DownloadState.ErrorType.Unknown -> InstagramUiState.ErrorType.Unknown
                        }
                        _uiState.value = InstagramUiState.DownloadError(
                            message = result.message,
                            type = errorType
                        )
                    }
                    DownloadState.Loading -> {}
                }
            } catch (e: Exception) {
                _uiState.value = InstagramUiState.DownloadError(
                    message = e.message ?: "An unexpected error occurred",
                    type = InstagramUiState.ErrorType.Unknown
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = InstagramUiState.Idle
    }
}