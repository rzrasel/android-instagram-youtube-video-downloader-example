package com.rzrasel.instagramvideodownload.instagramdownloader.domain.state

sealed class SaveState {
    data class Success(val message: String) : SaveState()
    data class Error(val message: String) : SaveState()
}