package com.rzrasel.youtubevideodownload.youtubevideodownload.presentation.composables.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rzrasel.youtubevideodownload.youtubevideodownload.presentation.composables.composables.ErrorMessage
import com.rzrasel.youtubevideodownload.youtubevideodownload.presentation.composables.composables.StreamListSection
import com.rzrasel.youtubevideodownload.youtubevideodownload.presentation.composables.composables.UrlInputSection
import com.rzrasel.youtubevideodownload.youtubevideodownload.presentation.viewmodel.YouTubeViewModel

@Composable
fun YouTubeDownloaderScreen(
    viewModel: YouTubeViewModel = hiltViewModel(),
    hasStoragePermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.networkState) {
        if (uiState.networkState.isSuccess && uiState.downloadingUrl == null) {
            Toast.makeText(context, "Download completed", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
        UrlInputSection(
            url = uiState.url,
            onUrlChange = { viewModel.onUrlChange(it) },
            onFetchStreams = { viewModel.extractStreams() },
            isLoading = uiState.networkState.isLoading && uiState.downloadingUrl == null
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.error != null -> {
                ErrorMessage(error = uiState.error)
                if (uiState.error?.contains("permission", ignoreCase = true) == true) {
                    Button(
                        onClick = onRequestPermission,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Grant Storage Permission")
                    }
                }
            }

            uiState.streams.isNotEmpty() -> StreamListSection(
                youTubeStreams = uiState.streams,
                onStreamClick = { url ->
                    viewModel.downloadVideo(url, hasStoragePermission)
                },
                downloadingUrl = uiState.downloadingUrl
            )

            uiState.networkState.isLoading && uiState.streams.isEmpty() -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}