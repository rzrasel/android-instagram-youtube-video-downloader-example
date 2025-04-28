package com.rzrasel.instagramvideodownload.core

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BlobVideoDownloader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tag = "BlobVideoDownloader"

    suspend fun downloadBlobVideo(blobUrl: String): ByteArray? {
        Log.w(tag, "Blob URL detected: $blobUrl")
        throw UnsupportedOperationException(
            "Blob URLs cannot be downloaded directly. Using server-side solution with instaloader."
        )
    }
}