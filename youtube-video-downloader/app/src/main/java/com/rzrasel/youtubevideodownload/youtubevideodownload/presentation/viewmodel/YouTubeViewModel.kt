package com.rzrasel.youtubevideodownload.youtubevideodownload.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rzrasel.youtubevideodownload.youtubevideodownload.domain.usecase.DownloadVideoUseCase
import com.rzrasel.youtubevideodownload.youtubevideodownload.domain.usecase.GetVideoStreamsUseCase
import com.rzrasel.youtubevideodownload.youtubevideodownload.domain.networkstate.NetworkState
import com.rzrasel.youtubevideodownload.youtubevideodownload.presentation.uistate.YouTubeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class YouTubeViewModel @Inject constructor(
    private val getVideoStreamsUseCase: GetVideoStreamsUseCase,
    private val downloadVideoUseCase: DownloadVideoUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(YouTubeUiState())
    val uiState: StateFlow<YouTubeUiState> = _uiState

    fun onUrlChange(newUrl: String) {
        _uiState.update { it.copy(url = newUrl) }
    }

    fun extractStreams() {
        _uiState.update {
            it.copy(
                networkState = NetworkState.Loading,
                error = null,
                streams = emptyList(),
                downloadingUrl = null
            )
        }

        viewModelScope.launch {
            try {
                val streams = getVideoStreamsUseCase(_uiState.value.url)
                _uiState.update {
                    it.copy(
                        streams = streams,
                        error = null,
                        networkState = NetworkState.Success
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error: ${e.message}",
                        streams = emptyList(),
                        networkState = NetworkState.Error
                    )
                }
            }
        }
    }

    fun downloadVideo(url: String, hasPermission: Boolean) {
        if (!hasPermission) {
            _uiState.update {
                it.copy(
                    error = "Storage permission required",
                    networkState = NetworkState.Error
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                downloadingUrl = url,
                networkState = NetworkState.Loading,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                val uri = downloadVideoUseCase(
                    url,
                    "video_${System.currentTimeMillis()}.mp4",
                    hasPermission
                )
                _uiState.update {
                    it.copy(
                        networkState = NetworkState.Success,
                        downloadingUrl = null,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Download failed: ${e.message}",
                        networkState = NetworkState.Error,
                        downloadingUrl = null
                    )
                }
            }
        }
    }
}
