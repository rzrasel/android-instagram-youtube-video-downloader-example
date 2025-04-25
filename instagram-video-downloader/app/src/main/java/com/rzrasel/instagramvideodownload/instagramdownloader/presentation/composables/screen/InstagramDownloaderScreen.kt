package com.rzrasel.instagramvideodownload.instagramdownloader.presentation.composables.screen

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.rzrasel.instagramvideodownload.instagramdownloader.presentation.composables.components.UrlInputSection
import com.rzrasel.instagramvideodownload.instagramdownloader.presentation.viewmodel.InstagramViewModel

@Composable
fun InstagramDownloaderScreen(
    hasStoragePermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val viewModel: InstagramViewModel = hiltViewModel()
    UrlInputSection(
        viewModel = viewModel,
        hasStoragePermission = hasStoragePermission,
        onRequestPermission = onRequestPermission
    )
}