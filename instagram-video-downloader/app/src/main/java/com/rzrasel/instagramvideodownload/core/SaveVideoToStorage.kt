package com.rzrasel.instagramvideodownload.core

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.rzrasel.instagramvideodownload.instagramdownloader.domain.state.SaveState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SaveVideoToStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun save(videoBytes: ByteArray, fileName: String): SaveState {
        val contentValues = createContentValues(fileName)
        val collection = getMediaCollectionUri()

        val uri = context.contentResolver.insert(collection, contentValues)
            ?: return SaveState.Error("Failed to create MediaStore record")

        return try {
            writeVideoToUri(context, uri, videoBytes, contentValues)
            SaveState.Success("Video saved as $fileName")
        } catch (e: Exception) {
            cleanupOnError(context, uri)
            SaveState.Error(e.message ?: "Failed to save video")
        }
    }

    private fun createContentValues(fileName: String): ContentValues {
        return ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }
    }

    private fun getMediaCollectionUri() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    }

    private fun writeVideoToUri(
        context: Context,
        uri: android.net.Uri,
        videoBytes: ByteArray,
        contentValues: ContentValues
    ) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(videoBytes)
        } ?: throw Exception("Failed to open output stream")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
            context.contentResolver.update(uri, contentValues, null, null)
        }
    }

    private fun cleanupOnError(context: Context, uri: android.net.Uri) {
        try {
            context.contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
            // Log error if needed
        }
    }
}