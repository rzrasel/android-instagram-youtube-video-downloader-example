package com.rzrasel.instagramvideodownload.instagramdownloader.domain.usecase

import com.rzrasel.instagramvideodownload.instagramdownloader.domain.repository.InstagramRepository
import com.rzrasel.instagramvideodownload.instagramdownloader.domain.state.DownloadState
import javax.inject.Inject

class DownloadInstagramVideoUseCase @Inject constructor(
    private val repository: InstagramRepository
) {
    suspend operator fun invoke(url: String): DownloadState = repository.downloadVideo(url)
}