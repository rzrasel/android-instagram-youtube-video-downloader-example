package com.rzrasel.instagramvideodownload.core

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.OutputStream
import javax.inject.Inject

class SaveVideoToStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun save(videoBytes: ByteArray, fileName: String): SaveResult {
        if (videoBytes.isEmpty()) {
            return SaveResult.Error("Video data is empty")
        }

        if (videoBytes.size > Constants.MAX_VIDEO_SIZE_BYTES) {
            return SaveResult.Error("Video file is too large")
        }

        val contentValues = createContentValues(fileName)
        val collection = getMediaCollectionUri()

        val uri = context.contentResolver.insert(collection, contentValues)
            ?: return SaveResult.Error("Failed to create MediaStore record")

        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(videoBytes)
                outputStream.flush()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)
                }

                SaveResult.Success("Video saved as $fileName")
            } ?: SaveResult.Error("Failed to open output stream")
        } catch (e: Exception) {
            cleanupOnError(uri)
            SaveResult.Error(e.message ?: "Failed to save video")
        }
    }

    private fun createContentValues(fileName: String): ContentValues {
        return ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH,
                Environment.DIRECTORY_MOVIES + "/InstagramDownloads")
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

    private fun cleanupOnError(uri: android.net.Uri) {
        try {
            context.contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
            Log.e("SaveVideoToStorage", "Error cleaning up failed save", e)
        }
    }

    sealed class SaveResult {
        data class Success(val message: String) : SaveResult()
        data class Error(val message: String) : SaveResult()
    }
}