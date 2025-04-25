package com.rzrasel.youtubevideodownload.youtubevideodownload.domain.usecase

import com.rzrasel.youtubevideodownload.youtubevideodownload.domain.model.YouTubeStream
import com.rzrasel.youtubevideodownload.youtubevideodownload.domain.repository.YouTubeRepository
import javax.inject.Inject

class GetVideoStreamsUseCase @Inject constructor(
    private val repository: YouTubeRepository
) {
    suspend operator fun invoke(videoUrl: String): List<YouTubeStream> {
        return repository.getVideoStreams(videoUrl)
    }
}