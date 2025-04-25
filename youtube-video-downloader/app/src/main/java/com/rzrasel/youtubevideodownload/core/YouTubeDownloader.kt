package com.rzrasel.youtubevideodownload.core

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class YouTubeDownloader(val context: Context) {

    suspend fun downloadVideo(
        url: String,
        fileName: String = "video_${System.currentTimeMillis()}.mp4",
        hasPermission: Boolean,
        onPermissionDenied: () -> Unit = {}
    ): Uri? {
        if (!hasPermission) {
            onPermissionDenied()
            return null
        }

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("YouTube Video Download")
            .setDescription("Downloading")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return withContext(Dispatchers.IO) {
            val downloadId = downloadManager.enqueue(request)
            waitForDownloadCompletion(downloadManager, downloadId)
        }
    }

    @SuppressLint("Range")
    private suspend fun waitForDownloadCompletion(
        downloadManager: DownloadManager,
        downloadId: Long
    ): Uri {
        var downloading = true
        while (downloading) {
            val q = DownloadManager.Query().setFilterById(downloadId)
            downloadManager.query(q).use { cursor ->
                if (cursor.moveToFirst()) {
                    when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_SUCCESSFUL -> downloading = false
                        DownloadManager.STATUS_FAILED -> throw Exception("Download failed")
                    }
                }
            }
            delay(500)
        }

        return downloadManager.getUriForDownloadedFile(downloadId)
            ?: throw Exception("Couldn't get downloaded file URI")
    }
}