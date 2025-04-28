package com.rzrasel.instagramvideodownload.instagramdownloader.presentation.composables.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.rzrasel.instagramvideodownload.instagramdownloader.presentation.uistate.InstagramUiState
import com.rzrasel.instagramvideodownload.instagramdownloader.presentation.viewmodel.InstagramViewModel

@Composable
fun UrlInputSection(
    viewModel: InstagramViewModel,
    hasStoragePermission: Boolean,
    onRequestPermission: () -> Unit
) {
    var url by remember { mutableStateOf(TextFieldValue("")) }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Instagram Video URL") },
            placeholder = { Text("https://www.instagram.com/reel/...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (url.text.isBlank()) {
                    Toast.makeText(context, "Please enter a URL", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (hasStoragePermission) {
                    viewModel.downloadVideo(url.text)
                } else {
                    onRequestPermission()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is InstagramUiState.Loading
        ) {
            if (uiState is InstagramUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Downloading...")
            } else {
                Text("Download Video")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is InstagramUiState.Idle -> {}
            is InstagramUiState.Loading -> {}
            is InstagramUiState.DownloadSuccess -> {
                Text(
                    text = (uiState as InstagramUiState.DownloadSuccess).message,
                    color = Color.Green,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            is InstagramUiState.DownloadError -> {
                val message = when ((uiState as InstagramUiState.DownloadError).type) {
                    InstagramUiState.ErrorType.InvalidUrl -> "Invalid Instagram URL. Please check the format."
                    InstagramUiState.ErrorType.PrivateAccount -> "Cannot download from a private account."
                    InstagramUiState.ErrorType.NoVideoContent -> "The post does not contain a video."
                    InstagramUiState.ErrorType.NetworkError -> "Network error. Please check your connection."
                    InstagramUiState.ErrorType.BlobUrl -> "Blob URL detected. Server processing failed."
                    InstagramUiState.ErrorType.StorageError -> "Failed to save video to storage."
                    else -> (uiState as InstagramUiState.DownloadError).message
                }
                Text(
                    text = message,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}