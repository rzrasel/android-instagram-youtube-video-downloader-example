package com.rzrasel.youtubevideodownload.youtubevideodownload.presentation.composables.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button

@Composable
fun UrlInputSection(
    url: String,
    onUrlChange: (String) -> Unit,
    onFetchStreams: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            label = { Text("YouTube URL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Button(
                    onClick = onFetchStreams,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fetch YouTube Video")
                }
            }
        }
    }
}