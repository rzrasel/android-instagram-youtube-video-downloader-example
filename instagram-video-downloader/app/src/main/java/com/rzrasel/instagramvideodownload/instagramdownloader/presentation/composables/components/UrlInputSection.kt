package com.rzrasel.instagramvideodownload.instagramdownloader.presentation.composables.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rzrasel.instagramvideodownload.instagramdownloader.presentation.uistate.InstagramUiState
import com.rzrasel.instagramvideodownload.instagramdownloader.presentation.viewmodel.InstagramViewModel

@Composable
fun UrlInputSection(
    viewModel: InstagramViewModel,
    hasStoragePermission: Boolean,
    onRequestPermission: () -> Unit
) {
    var url by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Instagram Video URL") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (hasStoragePermission) {
                    viewModel.downloadVideo(url)
                } else {
                    onRequestPermission()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is InstagramUiState.Loading
        ) {
            Text("Download Video")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            InstagramUiState.Idle -> {}
            InstagramUiState.Loading -> CircularProgressIndicator()
            is InstagramUiState.DownloadSuccess ->
                Text((uiState as InstagramUiState.DownloadSuccess).message, color = Color.Green)
            is InstagramUiState.DownloadError ->
                Text((uiState as InstagramUiState.DownloadError).message, color = Color.Red)
        }
    }
}