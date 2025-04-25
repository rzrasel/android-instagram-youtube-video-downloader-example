package com.rzrasel.youtubevideodownload.youtubevideodownload.presentation.composables.composables

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun ErrorMessage(
    error: String?,
    modifier: Modifier = Modifier
) {
    if (error != null) {
        Text(
            text = "Error: $error",
            color = Color.Red,
            modifier = modifier
        )
    }
}