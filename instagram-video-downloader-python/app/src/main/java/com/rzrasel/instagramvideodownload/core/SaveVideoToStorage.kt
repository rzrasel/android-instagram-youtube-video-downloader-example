package com.rzrasel.instagramvideodownload.core

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.rzrasel.instagramvideodownload.instagramdownloader.domain.state.SaveState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import java.io.OutputStream

class SaveVideoToStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun save(videoBytes: ByteArray, fileName: String): SaveState {
        if (videoBytes.size > Constants.MAX_VIDEO_SIZE_BYTES) {
            return SaveState.Error("Video file is too large")
        }

        val contentValues = createContentValues(fileName)
        val collection = getMediaCollectionUri()

        val uri = context.contentResolver.insert(collection, contentValues)
            ?: return SaveState.Error("Failed to create MediaStore record")

        var outputStream: OutputStream? = null
        return try {
            outputStream = context.contentResolver.openOutputStream(uri)
                ?: return SaveState.Error("Failed to open output stream")

            outputStream.write(videoBytes)
            outputStream.flush()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
            }

            SaveState.Success("Video saved as $fileName")
        } catch (e: Exception) {
            SaveState.Error(e.message ?: "Failed to save video").also {
                cleanupOnError(context, uri)
            }
        } finally {
            outputStream?.close()
        }
    }

    private fun createContentValues(fileName: String): ContentValues {
        return ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/InstagramDownloads")
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

    private fun cleanupOnError(context: Context, uri: android.net.Uri) {
        try {
            context.contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
            Log.e("SaveVideoToStorage", "Error cleaning up failed save", e)
        }
    }
}