package com.rzrasel.youtubevideodownload.youtubevideodownload.domain.usecase

import android.net.Uri
import com.rzrasel.youtubevideodownload.youtubevideodownload.domain.repository.YouTubeRepository
import javax.inject.Inject

class DownloadVideoUseCase @Inject constructor(
    private val repository: YouTubeRepository
) {
    suspend operator fun invoke(url: String, fileName: String, hasPermission: Boolean): Uri? {
        return repository.downloadVideo(url, fileName, hasPermission)
    }
}