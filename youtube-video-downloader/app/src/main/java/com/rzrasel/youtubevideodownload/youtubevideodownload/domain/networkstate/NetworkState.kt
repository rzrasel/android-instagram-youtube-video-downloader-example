package com.rzrasel.youtubevideodownload.youtubevideodownload.domain.networkstate

sealed class NetworkState {
    data object Idle : NetworkState()
    data object Loading : NetworkState()
    data object Success : NetworkState()
    data object Error : NetworkState()

    val isIdle: Boolean get() = this is Idle
    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
}
