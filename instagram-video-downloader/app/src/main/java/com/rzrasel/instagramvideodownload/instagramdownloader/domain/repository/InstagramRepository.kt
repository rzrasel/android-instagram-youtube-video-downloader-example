package com.rzrasel.instagramvideodownload.instagramdownloader.domain.repository

import com.rzrasel.instagramvideodownload.instagramdownloader.domain.state.DownloadState

interface InstagramRepository {
    suspend fun downloadVideo(url: String): DownloadState
}