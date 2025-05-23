package com.rzrasel.instagramvideodownload.instagramdownloader.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rzrasel.instagramvideodownload.instagramdownloader.domain.state.DownloadState
import com.rzrasel.instagramvideodownload.instagramdownloader.domain.usecase.DownloadInstagramVideoUseCase
import com.rzrasel.instagramvideodownload.instagramdownloader.presentation.uistate.InstagramUiState
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun downloadVideo(url: String) {
        viewModelScope.launch {
            _uiState.value = InstagramUiState.Loading
            when (val result = downloadUseCase(url)) {
                is DownloadState.Success -> {
                    _uiState.value = InstagramUiState.Success(result.message)
                }
                is DownloadState.Error -> {
                    _uiState.value = InstagramUiState.Error(result.message, result.type)
                }
            }
        }
    }
}