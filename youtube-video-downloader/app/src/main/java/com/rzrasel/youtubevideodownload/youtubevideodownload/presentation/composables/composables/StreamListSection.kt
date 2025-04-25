package com.rzrasel.youtubevideodownload.youtubevideodownload.presentation.composables.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rzrasel.youtubevideodownload.youtubevideodownload.domain.model.YouTubeStream

@Composable
fun StreamListSection(
    youTubeStreams: List<YouTubeStream>,
    onStreamClick: (String) -> Unit,
    downloadingUrl: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        Text("Available Streams:")

        Spacer(modifier = Modifier.height(8.dp))

        youTubeStreams.forEach { stream ->
            StreamCard(
                youTubeStream = stream,
                onClick = { onStreamClick(stream.url) },
                isDownloading = downloadingUrl == stream.url,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun StreamCard(
    youTubeStream: YouTubeStream,
    onClick: () -> Unit,
    isDownloading: Boolean,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text("Quality: ${youTubeStream.quality}")
            Text("Resolution: ${youTubeStream.resolution}")

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isDownloading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Button(
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Download Video")
                    }
                }
            }
        }
    }
}