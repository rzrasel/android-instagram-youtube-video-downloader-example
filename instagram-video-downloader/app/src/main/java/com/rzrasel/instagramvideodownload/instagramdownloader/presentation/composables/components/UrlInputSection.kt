package com.rzrasel.instagramvideodownload.instagramdownloader.presentation.composables.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.rzrasel.instagramvideodownload.instagramdownloader.presentation.uistate.InstagramUiState

@Composable
fun UrlInputSection(
    uiState: InstagramUiState,
    hasStoragePermission: Boolean,
    onDownloadClick: (String) -> Unit
) {
    var url by remember { mutableStateOf(TextFieldValue()) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Instagram URL") },
            singleLine = true,
            placeholder = { Text("https://www.instagram.com/reel/...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onDownloadClick(url.text) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is InstagramUiState.Loading && url.text.isNotBlank()
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
            is InstagramUiState.Success -> {
                Text(
                    text = uiState.message,
                    color = Color.Green,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            is InstagramUiState.Error -> {
                Text(
                    text = uiState.message,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            else -> {}
        }
    }
}