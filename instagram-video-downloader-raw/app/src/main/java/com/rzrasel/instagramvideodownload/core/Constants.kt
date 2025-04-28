package com.rzrasel.instagramvideodownload.core

object Constants {
    const val BASE_URL = "https://www.instagram.com/"
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
    const val VIDEO_FILE_PREFIX = "instagram_"
    const val VIDEO_FILE_EXTENSION = ".mp4"
    const val MAX_VIDEO_SIZE_BYTES = 50 * 1024 * 1024
    const val BLOB_DOWNLOAD_TIMEOUT = 60_000L
}