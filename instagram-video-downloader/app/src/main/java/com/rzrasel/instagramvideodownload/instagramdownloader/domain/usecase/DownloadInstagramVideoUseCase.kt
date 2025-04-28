package com.rzrasel.instagramvideodownload.instagramdownloader.domain.usecase

import com.rzrasel.instagramvideodownload.instagramdownloader.domain.repository.InstagramRepository
import javax.inject.Inject

class DownloadInstagramVideoUseCase @Inject constructor(
    private val repository: InstagramRepository
) {
    suspend operator fun invoke(url: String) = repository.downloadVideo(url)
}