package com.rzrasel.instagramvideodownload.instagramdownloader.presentation.composables.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rzrasel.instagramvideodownload.instagramdownloader.presentation.composables.components.UrlInputSection
import com.rzrasel.instagramvideodownload.instagramdownloader.presentation.viewmodel.InstagramViewModel

@Composable
fun InstagramDownloaderScreen(
    hasStoragePermission: Boolean,
    onRequestPermission: () -> Unit,
    viewModel: InstagramViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UrlInputSection(
                uiState = uiState,
                hasStoragePermission = hasStoragePermission,
                onDownloadClick = { url ->
                    if (hasStoragePermission) {
                        viewModel.downloadVideo(url)
                    } else {
                        onRequestPermission()
                    }
                }
            )
        }
    }
}